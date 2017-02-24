package it.unibz.inf.ontop.exception;

/**
 * Exception thrown while creating or closing a connection/statement
 *
 * TODO: find a better name
 */
public class OntopConnectionException extends OntopQueryEngineException {
    public OntopConnectionException(String message) {
        super(message);
    }

    public OntopConnectionException(String message, Exception e) {
        super(message, e);
    }

    public OntopConnectionException(Exception e) {
        super(e);
    }
}
