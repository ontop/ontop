package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

@Singleton
public class DistinctNormalizerImpl implements DistinctNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private DistinctNormalizerImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
    }


    /**
     * TODO: refactor
     */
    @Override
    public IQTree normalizeForOptimization(DistinctNode distinctNode, IQTree child,
                                           VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = child.removeDistincts();
        return liftBinding(distinctNode, newChild, variableGenerator, currentIQProperties);
    }

    /**
     * TODO: refactor
     */
    private IQTree liftBinding(DistinctNode distinctNode, IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = child.normalizeForOptimization(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftBindingConstructionChild(distinctNode, (ConstructionNode) newChildRoot, currentIQProperties, (UnaryIQTree) newChild);
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else
            return iqFactory.createUnaryIQTree(distinctNode, newChild, currentIQProperties.declareNormalizedForOptimization());
    }

    private IQTree liftBindingConstructionChild(DistinctNode distinctNode, ConstructionNode constructionNode, IQProperties currentIQProperties, UnaryIQTree child) {

        IQProperties liftedProperties = currentIQProperties.declareNormalizedForOptimization();

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = constructionNode.getSubstitution();

        IQTree grandChild = child.getChild();
        VariableNullability grandChildVariableNullability = grandChild.getVariableNullability();

        ImmutableMap<Boolean, ImmutableMap<Variable, ImmutableTerm>> partition =
                initialSubstitution.getImmutableMap().entrySet().stream()
                        .collect(ImmutableCollectors.partitioningBy(
                                e -> isLiftable(e.getValue(), grandChildVariableNullability),
                                ImmutableCollectors.toMap()));

        Optional<ConstructionNode> liftedConstructionNode = Optional.ofNullable(partition.get(true))
                .filter(m -> !m.isEmpty())
                .map(substitutionFactory::getSubstitution)
                .map(s -> iqFactory.createConstructionNode(child.getVariables(), s));

        ImmutableSet<Variable> newChildVariables = liftedConstructionNode
                .map(ConstructionNode::getChildVariables)
                .orElseGet(child::getVariables);

        IQTree newChild = Optional.ofNullable(partition.get(false))
                .filter(m -> !m.isEmpty())
                .map(substitutionFactory::getSubstitution)
                .map(s -> iqFactory.createConstructionNode(newChildVariables, s))
                .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, grandChild, liftedProperties))
                .orElseGet(() -> newChildVariables.equals(grandChild.getVariables())
                        ? grandChild
                        : iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(newChildVariables),
                        grandChild, liftedProperties));

        IQTree distinctTree = iqFactory.createUnaryIQTree(distinctNode, newChild, liftedProperties);

        return liftedConstructionNode
                .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, distinctTree, liftedProperties))
                .orElse(distinctTree);
    }

    /**
     *
     * NULL is treated as a regular constant (consistent with SPARQL DISTINCT and apparently with SQL DISTINCT)
     *
     */
    private boolean isLiftable(ImmutableTerm value, VariableNullability variableNullability) {
        if (value instanceof VariableOrGroundTerm)
            return true;
        // TODO: refactor
        return ((ImmutableFunctionalTerm) value).getFunctionSymbol().isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms();
        //return ((ImmutableFunctionalTerm) value).isInjective(variableNullability);
    }
}
