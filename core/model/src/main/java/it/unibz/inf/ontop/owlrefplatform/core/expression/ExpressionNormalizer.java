package it.unibz.inf.ontop.owlrefplatform.core.expression;

import it.unibz.inf.ontop.model.ImmutableExpression;

/**
 * Normalizes expressions
 */
public interface ExpressionNormalizer {

    ImmutableExpression normalize(ImmutableExpression expression);
}
