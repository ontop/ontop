package it.unibz.inf.ontop.exception;


public class UnknownDatatypeException extends MappingException {
    public UnknownDatatypeException(String message) {
        super(message);
    }
    public UnknownDatatypeException(UnknownDatatypeException e, String message) {
        super(e.getMessage() + message, e);
    }
}
