package it.unibz.inf.ontop.pivotalrepr.validation;

/**
 * Thrown by validators
 */
public class InvalidIntermediateQueryException extends RuntimeException {
    public InvalidIntermediateQueryException(String message) {
        super(message);
    }
}
