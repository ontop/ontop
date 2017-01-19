package it.unibz.inf.ontop.nativeql;

/**
 * Exception while interacting with the DB
 */
public class DBException extends Exception {

    public DBException(String message) {
        super(message);
    }
}
