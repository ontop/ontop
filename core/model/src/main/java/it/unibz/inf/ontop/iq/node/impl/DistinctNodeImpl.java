package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class DistinctNodeImpl extends QueryModifierNodeImpl implements DistinctNode {

    private static final String DISTINCT_NODE_STR = "DISTINCT";
    private final SubstitutionFactory substitutionFactory;

    @Inject
    private DistinctNodeImpl(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        super(iqFactory);
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree liftBinding(IQTree child, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        IQTree newChild = child.liftBinding(variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        if (newChildRoot instanceof ConstructionNode)
            return liftBindingConstructionChild(newChild, (ConstructionNode) newChildRoot, currentIQProperties);
        else if (newChildRoot instanceof DistinctNode)
            return newChild;
        else if (newChildRoot instanceof EmptyNode)
            return newChild;
        else
            return iqFactory.createUnaryIQTree(this, newChild, currentIQProperties.declareLifted());
    }

    private IQTree liftBindingConstructionChild(IQTree child, ConstructionNode constructionNode,
                                                IQProperties currentIQProperties) {

        IQProperties liftedProperties = currentIQProperties.declareLifted();

        ImmutableSubstitution<ImmutableTerm> initialSubstitution = constructionNode.getSubstitution();

        ImmutableMap<Boolean, ImmutableMap<Variable, ImmutableTerm>> partition =
                initialSubstitution.getImmutableMap().entrySet().stream()
                .collect(ImmutableCollectors.partitioningBy(
                        e -> isLiftable(e.getValue()),
                        ImmutableCollectors.toMap()));

        Optional<ConstructionNode> liftedConstructionNode = Optional.ofNullable(partition.get(true))
                .filter(m -> !m.isEmpty())
                .map(substitutionFactory::getSubstitution)
                .map(s -> iqFactory.createConstructionNode(child.getVariables(), s));

        ImmutableSet<Variable> newChildVariables = liftedConstructionNode
                .map(ConstructionNode::getChildVariables)
                .orElseGet(child::getVariables);

        IQTree grandChild = ((UnaryIQTree) child).getChild();

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

        IQTree distinctTree = iqFactory.createUnaryIQTree(this, newChild, liftedProperties);

        return liftedConstructionNode
                .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, distinctTree, liftedProperties))
                .orElse(distinctTree);
    }

    /**
     * TODO: return true for injective functions
     *
     * NULL is treated as a regular constant (consistent with SPARQL DISTINCT and apparently with SQL DISTINCT)
     *
     */
    private boolean isLiftable(ImmutableTerm value) {
        // TODO: re-enable it
//        if (value instanceof VariableOrGroundTerm)
//            return true;
        // TODO: support injective functions
        return false;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitution(descendingSubstitution, constraint));
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution));
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformDistinct(tree, this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public DistinctNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return node instanceof DistinctNode;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return queryNode instanceof DistinctNode;
    }

    @Override
    public String toString() {
        return DISTINCT_NODE_STR;
    }

    @Override
    public DistinctNode clone() {
        return iqFactory.createDistinctNode();
    }
}
