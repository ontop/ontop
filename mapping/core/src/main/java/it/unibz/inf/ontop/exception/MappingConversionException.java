package it.unibz.inf.ontop.exception;

public class MappingConversionException extends OntopInternalBugException {

    protected MappingConversionException(String message) {
        super(message);
    }

    public MappingConversionException(Exception e) {
        super(e.getMessage());
    }
}
