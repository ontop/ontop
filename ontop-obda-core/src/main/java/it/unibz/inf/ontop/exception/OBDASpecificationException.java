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
}
