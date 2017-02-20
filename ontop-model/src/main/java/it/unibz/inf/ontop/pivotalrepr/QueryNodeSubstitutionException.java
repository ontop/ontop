package it.unibz.inf.ontop.pivotalrepr;

import it.unibz.inf.ontop.exception.OntopIllegalStateException;

/**
 * Thrown when the substitution cannot be applied to a QueryNode
 * because the application will not produce a node of the same type.
 *
 * Useful for some extensions of Ontop.
 */
public class QueryNodeSubstitutionException extends OntopIllegalStateException {
    public QueryNodeSubstitutionException(String message) {
        super(message);
    }
}
