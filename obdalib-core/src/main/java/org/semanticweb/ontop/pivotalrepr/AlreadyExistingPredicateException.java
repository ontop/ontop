package org.semanticweb.ontop.pivotalrepr;

/**
 * When the predicate is already used in the intermediate query
 * (in a construction node or in a data node)
 */
public class AlreadyExistingPredicateException extends RuntimeException {
    public AlreadyExistingPredicateException(String message) {
        super(message);
    }
}
