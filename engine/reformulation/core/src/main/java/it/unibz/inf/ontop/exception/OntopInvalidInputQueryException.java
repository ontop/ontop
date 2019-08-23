package it.unibz.inf.ontop.exception;

public class OntopInvalidInputQueryException extends OntopReformulationException {

    public OntopInvalidInputQueryException(String message) {
        super(message);
    }

    public OntopInvalidInputQueryException(Exception e) {
        super(e);
    }
}
