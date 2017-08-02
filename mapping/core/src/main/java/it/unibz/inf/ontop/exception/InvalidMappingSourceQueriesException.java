package it.unibz.inf.ontop.exception;


/**
 * Problems detected in the source queries of mapping assertions
 */
public class InvalidMappingSourceQueriesException extends InvalidMappingException {
    public InvalidMappingSourceQueriesException(String message) {
        super(message);
    }
}
