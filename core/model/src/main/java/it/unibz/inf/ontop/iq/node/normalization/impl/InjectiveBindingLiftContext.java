package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;


import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

/**
 * Out of a child construction node and a grand child tree, tries to lift injective definitions above
 * (that is inside ancestor construction nodes).
 *
 * Typically used when the implicit "central" node (the parent of the child construction node) is a DISTINCT.
 * Also used for the normalization of AggregationNodes.
 *
 */
public class InjectiveBindingLiftContext {

    protected final IQTreeTools iqTreeTools;
    protected final SubstitutionFactory substitutionFactory;

    protected final VariableGenerator variableGenerator;

    public InjectiveBindingLiftContext(VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.variableGenerator = variableGenerator;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected class InjectiveBindingLiftState {

        private final UnaryOperatorSequence<ConstructionNode> ancestors;
        private final Optional<ConstructionNode> optionalChildConstructionNode;
        // First descendent tree not starting with a construction node
        private final IQTree grandChildTree;

        /**
         * Initial state
         */
        protected InjectiveBindingLiftState(ConstructionNode childConstructionNode, IQTree grandChildTree) {
            this(UnaryOperatorSequence.of(), Optional.of(childConstructionNode), grandChildTree);
        }

        private InjectiveBindingLiftState(UnaryOperatorSequence<ConstructionNode> ancestors,
                                          Optional<ConstructionNode> optionalChildConstructionNode, IQTree grandChildTree) {
            this.ancestors = ancestors;
            this.grandChildTree = grandChildTree;
            this.optionalChildConstructionNode = optionalChildConstructionNode;
        }

        protected IQTree getGrandChildTree() {
            return grandChildTree;
        }

        protected Optional<ConstructionNode> getChildConstructionNode() {
            return optionalChildConstructionNode;
        }

        protected UnaryOperatorSequence<ConstructionNode> getAncestors() {
            return ancestors;
        }

        protected Optional<InjectiveBindingLiftState> liftBindings() {
            if (optionalChildConstructionNode.isEmpty())
                return Optional.empty();

            ConstructionNode childConstructionNode = optionalChildConstructionNode.get();

            Substitution<ImmutableTerm> childSubstitution = childConstructionNode.getSubstitution();
            if (childSubstitution.isEmpty())
                return Optional.empty();

            VariableNullability grandChildVariableNullability = grandChildTree.getVariableNullability();
            ImmutableSet<Variable> nonFreeVariables = childConstructionNode.getVariables();

            ImmutableMap<Variable, ImmutableFunctionalTerm.FunctionalTermDecomposition> injectivityDecompositionMap =
                    childSubstitution.builder()
                            .restrictRangeTo(ImmutableFunctionalTerm.class)
                            .toMapIgnoreOptional((v, t) -> t.analyzeInjectivity(nonFreeVariables, grandChildVariableNullability, variableGenerator));

            Substitution<ImmutableTerm> liftedSubstitution = substitutionFactory.union(
                    // All variables and constants
                    childSubstitution.restrictRangeTo(NonFunctionalTerm.class),
                    // (Possibly decomposed) injective functional terms
                    childSubstitution.builder()
                            .<ImmutableTerm>restrictRangeTo(ImmutableFunctionalTerm.class)
                            .transformOrRemove(injectivityDecompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getLiftableTerm)
                            .build());

            Optional<ConstructionNode> liftedConstructionNode = iqTreeTools.createOptionalConstructionNode(
                    childConstructionNode::getVariables, liftedSubstitution);

            ImmutableSet<Variable> newChildVariables = liftedConstructionNode
                    .map(ConstructionNode::getChildVariables)
                    .orElseGet(childConstructionNode::getVariables);

            Substitution<ImmutableFunctionalTerm> newChildSubstitution = childSubstitution.builder()
                    .restrictRangeTo(ImmutableFunctionalTerm.class)
                    .flatTransform(injectivityDecompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getSubstitution)
                    .build();

            var newChildConstructionNode =
                    iqTreeTools.createOptionalConstructionNode(newChildVariables, newChildSubstitution, grandChildTree);

            // Nothing lifted
            if (newChildConstructionNode.equals(optionalChildConstructionNode)) {
                if (liftedConstructionNode.isPresent())
                    throw new MinorOntopInternalBugException("Unexpected lifted construction node");
                return Optional.empty();
            }

            return Optional.of(new InjectiveBindingLiftState(
                    ancestors.append(liftedConstructionNode
                            .orElseThrow(() -> new MinorOntopInternalBugException("A lifted construction node was expected"))),
                    newChildConstructionNode,
                    grandChildTree));
        }
    }
}