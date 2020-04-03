package it.unibz.inf.ontop.exception;

/**
 * Exception while extracting the DB metadata
 */
public class DBMetadataExtractionException extends OBDASpecificationException {

    public DBMetadataExtractionException(String message) {
        super(message);
    }

    public DBMetadataExtractionException(Exception e) {
        super(e);
    }

    public DBMetadataExtractionException(String prefix, Exception e) {
        super(prefix, e);
    }
}
