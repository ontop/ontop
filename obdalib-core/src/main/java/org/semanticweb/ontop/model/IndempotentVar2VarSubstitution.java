package org.semanticweb.ontop.model;

/**
 * Var-to-var substitution S satisfying the indempotency property:
 *
 * S(x) = S(S(x) = S(S(S(...S(x)...)))
 *
 * Concretely it means that its domain and its range do not intersect.
 *
 */
public interface IndempotentVar2VarSubstitution extends Var2VarSubstitution {
}
