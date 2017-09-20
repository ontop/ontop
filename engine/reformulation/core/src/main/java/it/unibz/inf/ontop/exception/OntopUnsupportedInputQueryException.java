package it.unibz.inf.ontop.exception;


public class OntopUnsupportedInputQueryException extends OntopReformulationException {

    public OntopUnsupportedInputQueryException(String message) {
        super(message);
    }

    public OntopUnsupportedInputQueryException(Exception e) {
        super(e);
    }

    public OntopUnsupportedInputQueryException(String message, Exception e) {
        super(message, e);
    }
}
