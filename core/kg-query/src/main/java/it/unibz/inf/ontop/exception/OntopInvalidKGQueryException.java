package it.unibz.inf.ontop.exception;

public class OntopInvalidKGQueryException extends OntopKGQueryException {

    public OntopInvalidKGQueryException(String message) {
        super(message);
    }

    public OntopInvalidKGQueryException(Exception e) {
        super(e);
    }
}
