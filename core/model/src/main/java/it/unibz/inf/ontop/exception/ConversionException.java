package it.unibz.inf.ontop.exception;

/**
 * Impossible to convert a substituted term in the expected type.
 */
public class ConversionException extends RuntimeException {

    public ConversionException(String message) {
        super(message);
    }
}
