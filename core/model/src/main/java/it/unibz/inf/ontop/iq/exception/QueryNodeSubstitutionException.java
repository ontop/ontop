package it.unibz.inf.ontop.iq.exception;

import it.unibz.inf.ontop.exception.OntopInternalBugException;

/**
 * Thrown when the substitution cannot be applied to a QueryNode
 * because the application will not produce a node of the same type.
 *
 * Useful for some extensions of Ontop.
 */
public class QueryNodeSubstitutionException extends OntopInternalBugException {
    public QueryNodeSubstitutionException(String message) {
        super(message);
    }
}
