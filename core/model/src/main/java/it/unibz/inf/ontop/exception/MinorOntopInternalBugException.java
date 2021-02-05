package it.unibz.inf.ontop.exception;

/**
 * For all of the minor exceptions that do not deserve a sub-class
 */
public class MinorOntopInternalBugException extends OntopInternalBugException {
    public MinorOntopInternalBugException(String message) {
        super(message);
    }

    public MinorOntopInternalBugException(String message, Throwable e) {
        super(message, e);
    }
}
