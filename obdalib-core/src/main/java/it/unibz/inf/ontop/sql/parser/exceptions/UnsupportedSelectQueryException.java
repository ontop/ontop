package it.unibz.inf.ontop.sql.parser.exceptions;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 * The exception is thrown when the query contains a construct
 * that is allowed in mappings but cannot be translated into
 * an internal representation.
 *
 */
public class UnsupportedSelectQueryException extends RuntimeException {

    public UnsupportedSelectQueryException(String message, Object object) {
        super(message + " "  + object.toString());
    }
}
