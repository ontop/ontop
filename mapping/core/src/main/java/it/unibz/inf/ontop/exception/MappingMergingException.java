package it.unibz.inf.ontop.exception;

public class MappingMergingException extends OntopInternalBugException {

    public MappingMergingException(String message) {
        super(message);
    }

    public MappingMergingException(Exception e) {
        super(e.getMessage());
    }
}
