package it.unibz.inf.ontop.exception;

public class MappingMergingException extends MappingException {

    public MappingMergingException(String message) {
        super(message);
    }

    public MappingMergingException(Exception e) {
        super(e);
    }
}
