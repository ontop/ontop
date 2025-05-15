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
public class InjectiveBindingLiftState {

    private final UnaryOperatorSequence<ConstructionNode> ancestors;
    @Nullable
    private final ConstructionNode childConstructionNode;
    // First descendent tree not starting with a construction node
    private final IQTree grandChildTree;

    private final VariableGenerator variableGenerator;

    private final CoreSingletons coreSingletons;
    private final IQTreeTools iqTreeTools;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    /**
     * Initial state
     */
    protected InjectiveBindingLiftState(@Nonnull ConstructionNode childConstructionNode, IQTree grandChildTree,
                                     VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        this(UnaryOperatorSequence.of(), grandChildTree, variableGenerator, childConstructionNode, coreSingletons);
    }

    private InjectiveBindingLiftState(UnaryOperatorSequence<ConstructionNode> ancestors, IQTree grandChildTree,
                                      VariableGenerator variableGenerator,
                                      ConstructionNode childConstructionNode, CoreSingletons coreSingletons) {
        this.ancestors = ancestors;
        this.grandChildTree = grandChildTree;
        this.childConstructionNode = childConstructionNode;
        this.variableGenerator = variableGenerator;

        this.coreSingletons = coreSingletons;
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    public IQTree getGrandChildTree() {
        return grandChildTree;
    }

    public Optional<ConstructionNode> getChildConstructionNode() {
        return Optional.ofNullable(childConstructionNode);
    }

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

        Substitution<ImmutableFunctionalTerm> newChildSubstitution =  childSubstitution.builder()
                .restrictRangeTo(ImmutableFunctionalTerm.class)
                .flatTransform(injectivityDecompositionMap::get, ImmutableFunctionalTerm.FunctionalTermDecomposition::getSubstitution)
                .build();

        var newChildConstructionNode =
                iqTreeTools.createOptionalConstructionNode(newChildVariables, newChildSubstitution, grandChildTree);

        // Nothing lifted
        if (newChildConstructionNode
                .filter(n -> n.equals(childConstructionNode))
                .isPresent()) {
            if (liftedConstructionNode.isPresent())
                throw new MinorOntopInternalBugException("Unexpected lifted construction node");
            return this;
        }

        return new InjectiveBindingLiftState(
                ancestors.append(liftedConstructionNode
                        .orElseThrow(() -> new MinorOntopInternalBugException("A lifted construction node was expected"))),
                grandChildTree,
                variableGenerator,
                newChildConstructionNode.orElse(null),
                coreSingletons);
    }
}
