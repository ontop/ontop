package it.unibz.inf.ontop.exception;

/**
 * When a mismatch is detected between the mapping and the T-Box (ontology)
 */
public class MappingOntologyMismatchException extends MappingException {

    public MappingOntologyMismatchException(String message) {
        super(message);
    }
}
