package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.ExtendedProjectionNodeImpl;
import it.unibz.inf.ontop.iq.node.impl.UnsatisfiableConditionException;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DownConstraint {
    private final Optional<ImmutableExpression> constraint;

    public DownConstraint(Optional<ImmutableExpression> constraint) {
        this.constraint = constraint;
    }

    public DownConstraint(ImmutableExpression constraint, VariableNullability variableNullability) throws UnsatisfiableConditionException {

        ImmutableExpression.Evaluation results = constraint.evaluate2VL(variableNullability);
        if (results.isEffectiveFalse())
            throw new UnsatisfiableConditionException();

        this.constraint = results.getExpression();
    }

    public DownConstraint() {
        this(Optional.empty());
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

    public Stream<Variable> getVariableStream() {
        return constraint.map(ImmutableExpression::getVariableStream)
                .orElseGet(Stream::empty);
    }
}
