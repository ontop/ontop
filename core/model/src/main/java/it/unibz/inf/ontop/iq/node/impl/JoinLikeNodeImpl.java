package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    protected JoinLikeNodeImpl(Optional<ImmutableExpression> optionalJoinCondition,
                               TermNullabilityEvaluator nullabilityEvaluator,
                               TermFactory termFactory, IntermediateQueryFactory iqFactory,
                               TypeFactory typeFactory, SubstitutionFactory substitutionFactory,
                               JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier,
                               IQTreeTools iqTreeTools) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, variableNullabilityTools, conditionSimplifier, iqTreeTools);
    }


    /**
     * Checks that non-projected variables are not shared among children
     */
    protected void checkNonProjectedVariables(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException {

        Set<Variable> allVariables = new HashSet<>();
        // Variables projected by the children
        children.forEach(c -> allVariables.addAll(c.getVariables()));

        for (IQTree child : children) {
            Set<Variable> childNonProjectedVariables = Sets.difference(child.getKnownVariables(), child.getVariables());

            Set<Variable> conflictingVariables = Sets.intersection(childNonProjectedVariables, allVariables);
            if (!conflictingVariables.isEmpty()) {
                throw new InvalidIntermediateQueryException("The following non-projected variables "
                        + conflictingVariables + " are appearing in different children of "+ this + ": \n"
                        + children.stream()
                            .filter(c -> !Sets.intersection(c.getKnownVariables(), conflictingVariables).isEmpty())
                            .map(c -> "\n" + c)
                            .collect(ImmutableCollectors.toList()));
            }
            allVariables.addAll(childNonProjectedVariables);
        }
    }

    protected VariableNonRequirement computeVariableNonRequirementForChildren(ImmutableList<IQTree> children) {
        ImmutableMultimap<Variable, ImmutableSet<Variable>> childRequirementMultimap = children.stream()
                .map(IQTree::getVariableNonRequirement)
                .flatMap(r -> r.getNotRequiredVariables().stream()
                        .map(v -> Maps.immutableEntry(v, r.getCondition(v))))
                .collect(ImmutableCollectors.toMultimap());

        ImmutableMap<Variable, ImmutableSet<Variable>> candidates = childRequirementMultimap.asMap().entrySet().stream()
                .filter(e -> e.getValue().size() == 1)
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().iterator().next()));

        // All variables are required
        if (candidates.isEmpty())
            return VariableNonRequirement.empty();

        ImmutableSet<Variable> notSharedVariables = NaryIQTreeTools.singleOccurrenceVariables(
                children.stream().map(IQTree::getVariables).flatMap(Collection::stream));

        return VariableNonRequirement.of(
                candidates.entrySet().stream()
                        .filter(e -> notSharedVariables.contains(e.getKey()))
                        .collect(ImmutableCollectors.toMap()));
    }
}
