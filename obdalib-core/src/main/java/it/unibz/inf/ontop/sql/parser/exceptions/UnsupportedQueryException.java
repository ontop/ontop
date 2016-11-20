package it.unibz.inf.ontop.sql.parser.exceptions;

/**
 * Created by Salvatore Rapisarda on 20/11/2016.
 *
 */
public class UnsupportedQueryException extends RuntimeException {

    public UnsupportedQueryException(String message, Object object) {
        super(message + " "  + object.toString());
    }
}
