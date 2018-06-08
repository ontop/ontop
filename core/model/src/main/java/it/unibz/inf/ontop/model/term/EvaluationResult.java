package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.model.term.impl.EvaluationResultImpl;

import java.util.Optional;

/**
 * TODO: explain
 *
 * 3-value logic
 *
 */
public interface EvaluationResult {

    enum Status {
        SAME_EXPRESSION,
        SIMPLIFIED_EXPRESSION,
        IS_NULL,
        IS_FALSE,
        IS_TRUE
    }

    /**
     * Only when getStatus() == SIMPLIFIED_EXPRESSION
     */
    Optional<ImmutableExpression> getSimplifiedExpression();

    Status getStatus();


    static EvaluationResult declareSimplifiedExpression(ImmutableExpression simplifiedExpression) {
        return EvaluationResultImpl.declareSimplifiedExpression(simplifiedExpression);
    }

    static EvaluationResult declareSameExpression() {
        return EvaluationResultImpl.declareSameExpression();
    }

    static EvaluationResult declareIsNull() {
        return EvaluationResultImpl.declareIsNull();
    }

    static EvaluationResult declareIsFalse() {
        return EvaluationResultImpl.declareIsFalse();
    }

    static EvaluationResult declareIsTrue() {
        return EvaluationResultImpl.declareIsTrue();
    }
}
