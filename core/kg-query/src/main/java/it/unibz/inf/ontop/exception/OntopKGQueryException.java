package it.unibz.inf.ontop.exception;

public class OntopKGQueryException extends Exception {
    public OntopKGQueryException(String message) {
        super(message);
    }

    public OntopKGQueryException(Exception e) {
        super(e);
    }

    public OntopKGQueryException(String message, Exception e) {
        super(message, e);
    }
}
