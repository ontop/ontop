package it.unibz.inf.ontop.iq.exception;

/**
 * Inconsistent query tree detected.
 *
 * Thrown by low-level operations.
 * Usually does not need to be expected.
 *
 */
public class IllegalTreeException extends RuntimeException {
    public IllegalTreeException(String message) {
        super(message);
    }
}
