package it.unibz.inf.ontop.exception;

/**
 * Exceptions due to internal bugs, not due to misconfiguration from the user or from a lack of support.
 *
 * To be derived.
 */
public abstract class OntopInternalBugException extends RuntimeException {

    protected OntopInternalBugException(String message) {
        super(message);
    }

    protected OntopInternalBugException(String message, Throwable e) {
        super(message, e);
    }
}
