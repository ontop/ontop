package it.unibz.inf.ontop.exception;

public class OntopQueryEvaluationException extends OntopQueryAnsweringException {

    public OntopQueryEvaluationException(String message) {
        super(message);
    }

    public OntopQueryEvaluationException(String message, Exception e) {
        super(message, e);
    }

    public OntopQueryEvaluationException(Exception e) {
        super(e);
    }
}
