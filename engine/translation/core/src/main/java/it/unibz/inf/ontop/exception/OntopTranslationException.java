package it.unibz.inf.ontop.exception;


/**
 * High-level exception occuring during query translation
 */
public class OntopTranslationException extends OntopQueryAnsweringException {

    protected OntopTranslationException(String message) {
        super(message);
    }

    public OntopTranslationException(Exception e) {
        super(e);
    }

    protected OntopTranslationException(String message, Exception e) {
        super(message, e);
    }
}
