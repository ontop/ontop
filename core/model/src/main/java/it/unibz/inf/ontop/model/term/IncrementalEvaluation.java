package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.model.term.impl.IncrementalEvaluationImpl;

import java.util.Optional;

/**
 * TODO: find a better name
 *
 * 3-value logic
 *
 */
public interface IncrementalEvaluation {

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
    Optional<ImmutableExpression> getNewExpression();

    Status getStatus();

    static IncrementalEvaluation declareSimplifiedExpression(ImmutableExpression simplifiedExpression) {
        return IncrementalEvaluationImpl.declareSimplifiedExpression(simplifiedExpression);
    }

    static IncrementalEvaluation declareSameExpression() {
        return IncrementalEvaluationImpl.declareSameExpression();
    }

    static IncrementalEvaluation declareIsNull() {
        return IncrementalEvaluationImpl.declareIsNull();
    }

    static IncrementalEvaluation declareIsFalse() {
        return IncrementalEvaluationImpl.declareIsFalse();
    }

    static IncrementalEvaluation declareIsTrue() {
        return IncrementalEvaluationImpl.declareIsTrue();
    }
}
