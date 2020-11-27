package it.unibz.inf.ontop.exception;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Exception while extracting the DB metadata
 */
public class MetadataExtractionException extends Exception {

    public MetadataExtractionException(String message) { super(message); }

    public MetadataExtractionException(Exception e) {
        super(e);
    }

    public MetadataExtractionException(String prefix, Exception e) {
        super(prefix, e);
    }

}
