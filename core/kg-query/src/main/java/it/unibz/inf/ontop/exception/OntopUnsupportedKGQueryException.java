package it.unibz.inf.ontop.exception;


public class OntopUnsupportedKGQueryException extends OntopKGQueryException {

    public OntopUnsupportedKGQueryException(String message) {
        super(message);
    }

    public OntopUnsupportedKGQueryException(Exception e) {
        super(e);
    }

    public OntopUnsupportedKGQueryException(String message, Exception e) {
        super(message, e);
    }
}
