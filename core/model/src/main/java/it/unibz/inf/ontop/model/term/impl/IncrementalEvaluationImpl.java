package it.unibz.inf.ontop.model.term.impl;

import it.unibz.inf.ontop.model.term.IncrementalEvaluation;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import javax.annotation.Nullable;
import java.util.Optional;

public class IncrementalEvaluationImpl implements IncrementalEvaluation {

    private static final IncrementalEvaluation SAME_EXPRESSION_RESULT, IS_NULL_RESULT, IS_TRUE_RESULT, IS_FALSE_RESULT;

    static {
        SAME_EXPRESSION_RESULT = new IncrementalEvaluationImpl(Status.SAME_EXPRESSION);
        IS_NULL_RESULT = new IncrementalEvaluationImpl(Status.IS_NULL);
        IS_FALSE_RESULT = new IncrementalEvaluationImpl(Status.IS_FALSE);
        IS_TRUE_RESULT = new IncrementalEvaluationImpl(Status.IS_TRUE);
    }

    @Nullable
    private final ImmutableExpression simplifiedExpression;
    private final Status status;

    private IncrementalEvaluationImpl(ImmutableExpression expression) {
        this.simplifiedExpression = expression;
        this.status = Status.SIMPLIFIED_EXPRESSION;
    }

    private IncrementalEvaluationImpl(Status status) {
        if (status == Status.SIMPLIFIED_EXPRESSION) {
            throw new IllegalArgumentException("Use a different construction for SIMPLIFIED EXPRESSION");
        }
        this.simplifiedExpression = null;
        this.status = status;
    }

    @Override
    public Optional<ImmutableExpression> getNewExpression() {
        return Optional.ofNullable(simplifiedExpression);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public static IncrementalEvaluation declareSimplifiedExpression(
            ImmutableExpression simplifiedExpression) {
        return new IncrementalEvaluationImpl(simplifiedExpression);
    }

    public static IncrementalEvaluation declareSameExpression() {
        return SAME_EXPRESSION_RESULT;
    }

    public static IncrementalEvaluation declareIsNull() {
        return IS_NULL_RESULT;
    }

    public static IncrementalEvaluation declareIsFalse() {
        return IS_FALSE_RESULT;
    }

    public static IncrementalEvaluation declareIsTrue() {
        return IS_TRUE_RESULT;
    }
}
