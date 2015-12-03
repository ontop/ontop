package org.semanticweb.ontop.pivotalrepr.transformer;


/**
 * Propagates the substitution down even to construction nodes.
 *
 * However, the latter may throw a NewSubstitutionException.
 */
public interface SubstitutionDownPropagator extends SubstitutionPropagator<UnificationException, NewSubstitutionException> {
}
