package it.unibz.inf.ontop.exception;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 * The exception may be thrown when the query contains a construct
 * that is not allowed in the mappings.
 *
 * Such a query cannot be translated into any internal representation.
 *
 */
public class InvalidQueryException extends Exception {

    public InvalidQueryException(String message) {
        super(message);
    }

    public InvalidQueryException(String message, Object object) {
        super(message + " "  + object);
    }
}
