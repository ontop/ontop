package it.unibz.inf.ontop.exception;

/**
 * When extracting the pre-processed implicit DB constraints
 */
public class ImplicitDBContraintException extends DBMetadataExtractionException {
    public ImplicitDBContraintException(String message) {
        super(message);
    }

    public ImplicitDBContraintException(Exception e) {
        super(e);
    }

    public ImplicitDBContraintException(String prefix, Exception e) {
        super(prefix, e);
    }
}
