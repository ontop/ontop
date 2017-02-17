package it.unibz.inf.ontop.exception;

public class OntopInvalidInputQueryException extends OntopQueryAnsweringException {

    public OntopInvalidInputQueryException(String message) {
        super(message);
    }

    public OntopInvalidInputQueryException(Exception e) {
        super(e);
    }
}
