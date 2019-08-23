package it.unibz.inf.ontop.exception;


public class MappingException extends OBDASpecificationException {

    protected MappingException(String message) {
        super(message);
    }

    protected MappingException(Exception e) {
        super(e);
    }
}
