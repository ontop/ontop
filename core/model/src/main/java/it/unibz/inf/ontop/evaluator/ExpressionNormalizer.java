package it.unibz.inf.ontop.evaluator;

import it.unibz.inf.ontop.model.term.ImmutableExpression;

/**
 * Normalizes expressions
 */
public interface ExpressionNormalizer {

    ImmutableExpression normalize(ImmutableExpression expression);
}
