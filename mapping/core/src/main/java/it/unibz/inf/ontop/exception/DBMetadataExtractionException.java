package it.unibz.inf.ontop.exception;

/**
 * Exception while extracting the DB metadata
 */
public class DBMetadataExtractionException extends OBDASpecificationException {

    public DBMetadataExtractionException(String message) {
        super(message);
    }

    protected DBMetadataExtractionException(Exception e) {
        super(e);
    }

    protected DBMetadataExtractionException(String prefix, Exception e) {
        super(prefix, e);
    }
}
