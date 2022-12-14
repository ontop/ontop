package it.unibz.inf.ontop.exception;


public class OntopResultConversionException extends OntopQueryAnsweringException {

    public OntopResultConversionException(String message) {
        super(message);
    }

    public OntopResultConversionException(String message, Exception e) {
        super(message, e);
    }

    public OntopResultConversionException(Exception e) {
        super(e);
    }
}
