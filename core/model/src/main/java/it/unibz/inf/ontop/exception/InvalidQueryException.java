package it.unibz.inf.ontop.exception;

/**
 *
 * May be thrown when the query contains a construct that is not allowed in the mappings.
 * <p>
 * Such a query cannot be translated into any internal representation.
 *
 */
public class InvalidQueryException extends Exception {

    public InvalidQueryException(String message) {
        super(message);
    }

    public InvalidQueryException(String message, Object object) {
        super(message + " (from "  + object + ")");
    }
}
