package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;

import javax.annotation.Nullable;
import java.util.Optional;

public class EvaluationResultImpl implements ImmutableFunctionalTerm.EvaluationResult {

    private static final ImmutableFunctionalTerm.EvaluationResult SAME_EXPRESSION_RESULT, IS_NULL_RESULT, IS_TRUE_RESULT, IS_FALSE_RESULT;

    static {
        SAME_EXPRESSION_RESULT = new EvaluationResultImpl(Status.SAME_EXPRESSION);
        IS_NULL_RESULT = new EvaluationResultImpl(Status.IS_NULL);
        IS_FALSE_RESULT = new EvaluationResultImpl(Status.IS_FALSE);
        IS_TRUE_RESULT = new EvaluationResultImpl(Status.IS_TRUE);
    }

    @Nullable
    private final ImmutableExpression simplifiedExpression;
    private final Status status;

    private EvaluationResultImpl(ImmutableExpression expression) {
        this.simplifiedExpression = expression;
        this.status = Status.SIMPLIFIED_EXPRESSION;
    }

    private EvaluationResultImpl(Status status) {
        if (status == Status.SIMPLIFIED_EXPRESSION) {
            throw new IllegalArgumentException("Use a different construction for SIMPLIFIED EXPRESSION");
        }
        this.simplifiedExpression = null;
        this.status = status;
    }

    @Override
    public Optional<ImmutableExpression> getSimplifiedExpression() {
        return Optional.ofNullable(simplifiedExpression);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public static ImmutableFunctionalTerm.EvaluationResult declareSimplifiedExpression(
            ImmutableExpression simplifiedExpression) {
        return new EvaluationResultImpl(simplifiedExpression);
    }

    public static ImmutableFunctionalTerm.EvaluationResult declareSameExpression() {
        return SAME_EXPRESSION_RESULT;
    }

    public static ImmutableFunctionalTerm.EvaluationResult declareIsNull() {
        return IS_NULL_RESULT;
    }

    public static ImmutableFunctionalTerm.EvaluationResult declareIsFalse() {
        return IS_FALSE_RESULT;
    }

    public static ImmutableFunctionalTerm.EvaluationResult declareIsTrue() {
        return IS_TRUE_RESULT;
    }
}
