package it.unibz.inf.ontop.exception;

/**
 * Thrown when loading the OBDA specification
 *
 * To be specialized
 *
 */
public class OBDASpecificationException extends Exception {

    protected OBDASpecificationException(String message) {
        super(message);
    }

    protected OBDASpecificationException(Exception e) {
        super(e);
    }

    protected OBDASpecificationException(String prefix, Exception e) {
        super(prefix, e);
    }
}
