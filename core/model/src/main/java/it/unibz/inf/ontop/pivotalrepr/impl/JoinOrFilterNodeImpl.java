package it.unibz.inf.ontop.pivotalrepr.impl;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;

import java.util.Optional;

public abstract class JoinOrFilterNodeImpl extends QueryNodeImpl implements JoinOrFilterNode {

    private Optional<ImmutableExpression> optionalFilterCondition;
    private final TermNullabilityEvaluator nullabilityEvaluator;

    protected JoinOrFilterNodeImpl(Optional<ImmutableExpression> optionalFilterCondition,
                                   TermNullabilityEvaluator nullabilityEvaluator) {
        this.optionalFilterCondition = optionalFilterCondition;
        this.nullabilityEvaluator = nullabilityEvaluator;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }

    protected String getOptionalFilterString() {
        if (optionalFilterCondition.isPresent()) {
            return " " + optionalFilterCondition.get().toString();
        }

        return "";
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        if (optionalFilterCondition.isPresent()) {
            return optionalFilterCondition.get().getVariables();
        }
        else {
            return ImmutableSet.of();
        }
    }

    protected ExpressionEvaluator.EvaluationResult transformBooleanExpression(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            ImmutableExpression booleanExpression) {

        ImmutableExpression substitutedExpression = substitution.applyToBooleanExpression(booleanExpression);

        return new ExpressionEvaluator().evaluateExpression(substitutedExpression);
    }

    protected boolean isFilteringNullValue(Variable variable) {
        return getOptionalFilterCondition()
                .filter(e -> nullabilityEvaluator.isFilteringNullValue(e, variable))
                .isPresent();
    }

    protected TermNullabilityEvaluator getNullabilityEvaluator() {
        return nullabilityEvaluator;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }
}
