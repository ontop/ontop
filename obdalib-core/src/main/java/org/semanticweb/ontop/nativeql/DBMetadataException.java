package org.semanticweb.ontop.nativeql;

/**
 * Exception occurring when extracting the DB metadata.
 *
 * TODO: find a more specific super class
 */
public class DBMetadataException extends Exception {

    public DBMetadataException(String message) {
        super(message);
    }
}
