package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DownConstraint {
    private final Optional<ImmutableExpression> constraint;

    public DownConstraint(Optional<ImmutableExpression> constraint) {
        this.constraint = constraint;
    }

    public DownConstraint() {
        this(Optional.empty());
    }


    public VariableNullability extendVariableNullability(VariableNullability variableNullability) {
        return constraint
                .map(c -> variableNullability.extendToExternalVariables(c.getVariableStream()))
                .orElse(variableNullability);
    }

    public IQTree propagateDownOptionalConstraint(IQTree child, VariableGenerator variableGenerator) {
        return constraint.map(c -> child.propagateDownConstraint(c, variableGenerator)).orElse(child);
    }

    public IQTree applyDescendingSubstitution(IQTree child, Substitution<? extends VariableOrGroundTerm> substitution, VariableGenerator variableGenerator) {
        return Optional.of(substitution)
                .filter(s -> !s.isEmpty())
                .map(s -> child.applyDescendingSubstitution(s, constraint, variableGenerator))
                .orElseGet(() -> propagateDownOptionalConstraint(child, variableGenerator));
    }

    public ImmutableList<IQTree> applyDescendingSubstitution(ImmutableList<IQTree> children, Substitution<? extends VariableOrGroundTerm> substitution, VariableGenerator variableGenerator) {
        return Optional.of(substitution)
                .filter(s -> !s.isEmpty())
                .map(s -> children.stream()
                        .map(c -> c.applyDescendingSubstitution(s, constraint, variableGenerator))
                        .collect(ImmutableCollectors.toList()))
                .or(() -> constraint
                        .map(cs -> children.stream()
                                .map(c -> c.propagateDownConstraint(cs, variableGenerator))
                                .collect(ImmutableCollectors.toList())))
                .orElse(children);
    }

    public Optional<ImmutableExpression> getConstraint() {
        return constraint;
    }
}
