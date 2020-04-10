package it.unibz.inf.ontop.spec.mapping.sqlparser.exception;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 * The exception is thrown when the query contains a construct
 * that is not allowed in the mappings.
 *
 * Such a query cannot be translated into any internal representation.
 *
 */
public class InvalidSelectQueryException extends Exception {

    public InvalidSelectQueryException(String message, Object object) {
        super(message + " "  + object);
    }
}
