package it.unibz.inf.ontop.exception;

/**
 * High-level exception occurring at runtime
 */
public abstract class OntopRuntimeException extends RuntimeException {
    protected OntopRuntimeException(String message) {
        super(message);
    }

    protected OntopRuntimeException(String message, Exception e) {
        super(message, e);
    }

    protected OntopRuntimeException(Exception e) {
        super(e);
    }
}
