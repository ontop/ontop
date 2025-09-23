package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Function;

/**
 * Out of a child construction node and a grand child tree, tries to lift injective definitions above
 * (that is inside ancestor construction nodes).
 *
 * Typically used when the implicit "central" node (the parent of the child construction node) is a DISTINCT.
 * Also used for the normalization of AggregationNodes.
 *
 */
public class InjectiveBindingLiftContext extends NormalizationContext {

    protected final IntermediateQueryFactory iqFactory;
    protected final SubstitutionFactory substitutionFactory;

    public InjectiveBindingLiftContext(ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, CoreSingletons coreSingletons, IQTreeCache treeCache) {
        super(projectedVariables, variableGenerator, treeCache, coreSingletons.getIQTreeTools());
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqFactory = coreSingletons.getIQFactory();
    }

    protected Optional<State<ConstructionNode, UnarySubTree<ConstructionNode>>> liftBindings(State<ConstructionNode, UnarySubTree<ConstructionNode>> state) {
        UnarySubTree<ConstructionNode> subTree = state.getSubTree();
        if (subTree.getOptionalNode().isEmpty())
            return Optional.empty();

        ConstructionNode constructionNode = subTree.getOptionalNode().get();

        Substitution<ImmutableTerm> substitution = constructionNode.getSubstitution();
        if (substitution.isEmpty())
            return Optional.empty();

        VariableNullability grandChildVariableNullability = subTree.getChild().getVariableNullability();
        ImmutableSet<Variable> nonFreeVariables = constructionNode.getVariables();

        SubstitutionSplitter injectivityDecomposition = new SubstitutionSplitter(substitution,
                t -> t.analyzeInjectivity(nonFreeVariables, grandChildVariableNullability, variableGenerator));

        Optional<ConstructionNode> liftedConstructionNode = iqTreeTools.createOptionalConstructionNode(
                constructionNode::getVariables,
                injectivityDecomposition.getLiftedSubstitution());

        ImmutableSet<Variable> newChildVariables = liftedConstructionNode
                .map(ConstructionNode::getChildVariables)
                .orElseGet(constructionNode::getVariables);

        var newOptionalConstructionNode =
                iqTreeTools.createOptionalConstructionNode(
                        newChildVariables,
                        injectivityDecomposition.getNonLiftedSubstitution(),
                        subTree.getChild());

        // Nothing lifted
        if (newOptionalConstructionNode.equals(subTree.getOptionalNode())) {
            if (liftedConstructionNode.isPresent())
                throw new MinorOntopInternalBugException("Unexpected lifted construction node");
            return Optional.empty();
        }

        return Optional.of(state.lift(
                liftedConstructionNode
                        .orElseThrow(() -> new MinorOntopInternalBugException("A lifted construction node was expected")),
                UnarySubTree.of(newOptionalConstructionNode, subTree.getChild())));
    }

    protected final class SubstitutionSplitter {
        private final Substitution<ImmutableTerm> substitution;
        private final ImmutableMap<Variable, ImmutableFunctionalTerm.FunctionalTermDecomposition> decompositionMap;

        protected SubstitutionSplitter(Substitution<ImmutableTerm> substitution, Function<ImmutableFunctionalTerm, Optional<ImmutableFunctionalTerm.FunctionalTermDecomposition>> decompositionFunction) {
            this.substitution = substitution;
            this.decompositionMap = substitution.builder()
                    .restrictRangeTo(ImmutableFunctionalTerm.class)
                    .toMapIgnoreOptional((v, t) -> decompositionFunction.apply(t));
        }

        protected Substitution<ImmutableTerm> getLiftedSubstitution() {
            return substitutionFactory.union(
                    // All variables and constants
                    substitution.<ImmutableTerm>restrictRangeTo(NonFunctionalTerm.class),
                    // (Possibly decomposed) functional terms
                    substitution.builder()
                            .<ImmutableTerm>restrictRangeTo(ImmutableFunctionalTerm.class)
                            .transformOrRemove(decompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getLiftableTerm)
                            .build());
        }

        protected Substitution<ImmutableFunctionalTerm> getNonLiftedSubstitution() {
            return substitution.builder()
                    .restrictRangeTo(ImmutableFunctionalTerm.class)
                    .flatTransform(decompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getSubstitution)
                    .build();
        }
    }
}
