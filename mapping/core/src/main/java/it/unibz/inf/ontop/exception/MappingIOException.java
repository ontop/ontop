package it.unibz.inf.ontop.exception;


public class MappingIOException extends MappingException {
    public MappingIOException(String message) {
        super(message);
    }

    public MappingIOException(Exception e) {
        super(e);
    }
}
