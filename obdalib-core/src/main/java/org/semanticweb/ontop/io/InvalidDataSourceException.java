package org.semanticweb.ontop.io;

/**
 * Information for creating an OBDADataSource is incomplete or invalid.
 */
public class InvalidDataSourceException extends Exception {
    public InvalidDataSourceException(String message) {
        super(message);
    }
}
