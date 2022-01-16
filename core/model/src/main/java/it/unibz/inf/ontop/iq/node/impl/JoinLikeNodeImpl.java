package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    protected JoinLikeNodeImpl(Optional<ImmutableExpression> optionalJoinCondition,
                               TermNullabilityEvaluator nullabilityEvaluator,
                               TermFactory termFactory, IntermediateQueryFactory iqFactory,
                               TypeFactory typeFactory, SubstitutionFactory substitutionFactory,
                               ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                               JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, unificationTools, substitutionTools, variableNullabilityTools, conditionSimplifier);
    }


    /**
     * Checks that non-projected variables are not shared among children
     */
    protected void checkNonProjectedVariables(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException {

        Set<Variable> allVariables = new HashSet<>();
        // Variables projected by the children
        children.forEach(c -> allVariables.addAll(c.getVariables()));

        for (IQTree child : children) {
            ImmutableSet<Variable> childProjectedVariables = child.getVariables();


            ImmutableSet<Variable> childNonProjectedVariables = child.getKnownVariables().stream()
                    .filter(v -> !childProjectedVariables.contains(v))
                    .collect(ImmutableCollectors.toSet());

            ImmutableSet<Variable> conflictingVariables = childNonProjectedVariables.stream()
                    .filter(allVariables::contains)
                    .collect(ImmutableCollectors.toSet());


            if (!conflictingVariables.isEmpty()) {
                throw new InvalidIntermediateQueryException("The following non-projected variables "
                        + conflictingVariables + " are appearing in different children of "+ this + ": \n"
                        + children.stream()
                            .filter(c -> c.getKnownVariables().stream()
                                    .anyMatch(conflictingVariables::contains))
                            .map(c -> "\n" + c.toString())
                            .collect(ImmutableCollectors.toList()));
            }
            allVariables.addAll(childNonProjectedVariables);
        }
    }
}
