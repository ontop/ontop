package it.unibz.inf.ontop.exception;

/**
 * Thrown when no input mapping has been provided.
 */
public class MissingInputMappingException extends OBDASpecificationException {

    public MissingInputMappingException() {
        super("An input mapping is required");
    }
}
