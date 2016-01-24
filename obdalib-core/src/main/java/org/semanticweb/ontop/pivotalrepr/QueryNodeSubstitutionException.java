package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.exception.OntopLowLevelException;

/**
 * Thrown when the substitution cannot be applied to a QueryNode
 * because the application will not produce a node of the same type.
 *
 * Useful for some extensions of Ontop.
 */
public class QueryNodeSubstitutionException extends OntopLowLevelException {
    public QueryNodeSubstitutionException(String message) {
        super(message);
    }
}
