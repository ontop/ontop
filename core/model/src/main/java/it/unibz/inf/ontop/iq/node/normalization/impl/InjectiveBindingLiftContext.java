package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
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

/**
 * Out of a child construction node and a grand child tree, tries to lift injective definitions above
 * (that is inside ancestor construction nodes).
 *
 * Typically used when the implicit "central" node (the parent of the child construction node) is a DISTINCT.
 * Also used for the normalization of AggregationNodes.
 *
 */
public class InjectiveBindingLiftContext extends NormalizationContext {

    protected final IQTreeTools iqTreeTools;
    protected final IntermediateQueryFactory iqFactory;
    protected final SubstitutionFactory substitutionFactory;

    protected final IQTreeCache treeCache;

    public InjectiveBindingLiftContext(VariableGenerator variableGenerator, CoreSingletons coreSingletons, IQTreeCache treeCache) {
        super(variableGenerator);
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.treeCache = treeCache;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class ConstructionSubTree {
        private final Optional<ConstructionNode> optionalConstructionNode;
        // First descendent tree not starting with a construction node
        private final IQTree child;

        public ConstructionSubTree(Optional<ConstructionNode> optionalConstructionNode, IQTree child) {
            this.optionalConstructionNode = optionalConstructionNode;
            this.child = child;
        }

        /**
         * Initial state
         */
        public ConstructionSubTree(ConstructionNode constructionNode, IQTree child) {
            this(Optional.of(constructionNode), child);
        }

        public Optional<ConstructionNode> getOptionalConstructionNode() {
            return optionalConstructionNode;
        }

        public IQTree getChild() {
            return child;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ConstructionSubTree) {
                ConstructionSubTree other = (ConstructionSubTree)o;
                return optionalConstructionNode.equals(other.optionalConstructionNode)
                        && child.equals(other.child);
            }
            return false;
        }
    }


    protected Optional<State<ConstructionNode, ConstructionSubTree>> liftBindings(State<ConstructionNode, ConstructionSubTree> state) {
        if (state.getSubTree().optionalConstructionNode.isEmpty())
            return Optional.empty();

        ConstructionNode constructionNode = state.getSubTree().optionalConstructionNode.get();

        Substitution<ImmutableTerm> substitution = constructionNode.getSubstitution();
        if (substitution.isEmpty())
            return Optional.empty();

        VariableNullability grandChildVariableNullability = state.getSubTree().child.getVariableNullability();
        ImmutableSet<Variable> nonFreeVariables = constructionNode.getVariables();

        ImmutableMap<Variable, ImmutableFunctionalTerm.FunctionalTermDecomposition> injectivityDecompositionMap =
                substitution.builder()
                        .restrictRangeTo(ImmutableFunctionalTerm.class)
                        .toMapIgnoreOptional((v, t) -> t.analyzeInjectivity(nonFreeVariables, grandChildVariableNullability, variableGenerator));

        Substitution<ImmutableTerm> liftedSubstitution = substitutionFactory.union(
                // All variables and constants
                substitution.restrictRangeTo(NonFunctionalTerm.class),
                // (Possibly decomposed) injective functional terms
                substitution.builder()
                        .<ImmutableTerm>restrictRangeTo(ImmutableFunctionalTerm.class)
                        .transformOrRemove(injectivityDecompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getLiftableTerm)
                        .build());

        Optional<ConstructionNode> liftedConstructionNode = iqTreeTools.createOptionalConstructionNode(
                constructionNode::getVariables, liftedSubstitution);

        ImmutableSet<Variable> newChildVariables = liftedConstructionNode
                .map(ConstructionNode::getChildVariables)
                .orElseGet(constructionNode::getVariables);

        Substitution<ImmutableFunctionalTerm> newChildSubstitution = substitution.builder()
                .restrictRangeTo(ImmutableFunctionalTerm.class)
                .flatTransform(injectivityDecompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getSubstitution)
                .build();

        var newOptionalConstructionNode =
                iqTreeTools.createOptionalConstructionNode(newChildVariables, newChildSubstitution, state.getSubTree().child);

        // Nothing lifted
        if (newOptionalConstructionNode.equals(state.getSubTree().optionalConstructionNode)) {
            if (liftedConstructionNode.isPresent())
                throw new MinorOntopInternalBugException("Unexpected lifted construction node");
            return Optional.empty();
        }

        return Optional.of(state.of(liftedConstructionNode
                        .orElseThrow(() -> new MinorOntopInternalBugException("A lifted construction node was expected")),
                new ConstructionSubTree(newOptionalConstructionNode, state.getSubTree().child)));
    }
}
