package it.unibz.inf.ontop.exception;

public class NotFullyTranslatableToNativeQueryException extends OntopReformulationException {
    public NotFullyTranslatableToNativeQueryException(String message) {
        super("Not fully translatable into a native query: " + message);
    }
}
