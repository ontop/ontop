package it.unibz.inf.ontop.exception;


public class OntopQueryAnsweringException extends OntopQueryEngineException {

    protected OntopQueryAnsweringException(String message) {
        super(message);
    }

    protected OntopQueryAnsweringException(String message, Exception e) {
        super(message, e);
    }

    public OntopQueryAnsweringException(Exception e) {
        super(e);
    }
}
