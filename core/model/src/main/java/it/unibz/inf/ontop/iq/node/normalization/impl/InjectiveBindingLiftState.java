package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;


import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

/**
 * Out of a child construction node and a grand child tree, tries to lift injective definitions above
 * (that is inside ancestor construction nodes).
 *
 * Typically used when the implicit "central" node (the parent of the child construction node) is a DISTINCT.
 * Also used for the normalization of AggregationNodes.
 *
 */
public class InjectiveBindingLiftState {

    // The oldest ancestor is first
    private final UnaryOperatorSequence<ConstructionNode> ancestors;
    // First descendent tree not starting with a construction node
    private final IQTree grandChildTree;
    @Nullable
    private final ConstructionNode childConstructionNode;
    private final VariableGenerator variableGenerator;
    private final CoreSingletons coreSingletons;

    /**
     * Initial state
     */
    protected InjectiveBindingLiftState(@Nonnull ConstructionNode childConstructionNode, IQTree grandChildTree,
                                     VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.ancestors = UnaryOperatorSequence.of();
        this.grandChildTree = grandChildTree;
        this.childConstructionNode = childConstructionNode;
        this.variableGenerator = variableGenerator;
    }

    private InjectiveBindingLiftState(UnaryOperatorSequence<ConstructionNode> ancestors, IQTree grandChildTree,
                                      VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        this.ancestors = ancestors;
        this.grandChildTree = grandChildTree;
        this.coreSingletons = coreSingletons;
        this.childConstructionNode = null;
        this.variableGenerator = variableGenerator;
    }

    private InjectiveBindingLiftState(UnaryOperatorSequence<ConstructionNode> ancestors, IQTree grandChildTree,
                                      VariableGenerator variableGenerator,
                                      @Nonnull ConstructionNode childConstructionNode, CoreSingletons coreSingletons) {
        this.ancestors = ancestors;
        this.grandChildTree = grandChildTree;
        this.childConstructionNode = childConstructionNode;
        this.variableGenerator = variableGenerator;
        this.coreSingletons = coreSingletons;
    }

    public IQTree getGrandChildTree() {
        return grandChildTree;
    }

    public Optional<ConstructionNode> getChildConstructionNode() {
        return Optional.ofNullable(childConstructionNode);
    }

    /**
     * The oldest ancestor is first
     */
    public UnaryOperatorSequence<ConstructionNode> getAncestors() {
        return ancestors;
    }

    public InjectiveBindingLiftState liftBindings() {
        if (childConstructionNode == null)
            return this;

        Substitution<ImmutableTerm> childSubstitution = childConstructionNode.getSubstitution();
        if (childSubstitution.isEmpty())
            return this;

        VariableNullability grandChildVariableNullability = grandChildTree.getVariableNullability();

        ImmutableSet<Variable> nonFreeVariables = childConstructionNode.getVariables();

        ImmutableMap<Variable, ImmutableFunctionalTerm.FunctionalTermDecomposition> injectivityDecompositionMap =
                childSubstitution.builder()
                        .restrictRangeTo(ImmutableFunctionalTerm.class)
                        .toMapIgnoreOptional((v, t) -> t.analyzeInjectivity(nonFreeVariables, grandChildVariableNullability, variableGenerator));

        SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();

        Substitution<ImmutableTerm> liftedSubstitution = substitutionFactory.union(
                // All variables and constants
                childSubstitution.restrictRangeTo(NonFunctionalTerm.class),
                // (Possibly decomposed) injective functional terms
                childSubstitution.builder()
                        .<ImmutableTerm>restrictRangeTo(ImmutableFunctionalTerm.class)
                        .transformOrRemove(injectivityDecompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getLiftableTerm)
                        .build());

        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();

        Optional<ConstructionNode> liftedConstructionNode = Optional.of(liftedSubstitution)
                .filter(s -> !s.isEmpty())
                .map(s -> iqFactory.createConstructionNode(childConstructionNode.getVariables(), s));

        ImmutableSet<Variable> newChildVariables = liftedConstructionNode
                .map(ConstructionNode::getChildVariables)
                .orElseGet(childConstructionNode::getVariables);

        Substitution<ImmutableFunctionalTerm> newChildSubstitution =  childSubstitution.builder()
                .restrictRangeTo(ImmutableFunctionalTerm.class)
                .flatTransform(injectivityDecompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getSubstitution)
                .build();

        Optional<ConstructionNode> newChildConstructionNode = Optional.of(newChildSubstitution)
                .filter(s -> !s.isEmpty())
                .map(s -> iqFactory.createConstructionNode(newChildVariables, s))
                .or(() -> newChildVariables.equals(grandChildTree.getVariables())
                        ? Optional.empty()
                        : Optional.of(iqFactory.createConstructionNode(newChildVariables)));

        // Nothing lifted
        if (newChildConstructionNode
                .filter(n -> n.equals(childConstructionNode))
                .isPresent()) {
            if (liftedConstructionNode.isPresent())
                throw new MinorOntopInternalBugException("Unexpected lifted construction node");
            return this;
        }

        UnaryOperatorSequence<ConstructionNode> newAncestors = liftedConstructionNode
                .map(ancestors::append)
                .orElseThrow(() -> new MinorOntopInternalBugException("A lifted construction node was expected"));

        return newChildConstructionNode
                .map(c -> new InjectiveBindingLiftState(newAncestors, grandChildTree, variableGenerator, c, coreSingletons))
                .orElseGet(() -> new InjectiveBindingLiftState(newAncestors, grandChildTree, variableGenerator, coreSingletons));
    }
}
