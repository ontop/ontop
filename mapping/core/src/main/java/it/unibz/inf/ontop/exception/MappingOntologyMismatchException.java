package it.unibz.inf.ontop.exception;

import org.apache.commons.rdf.api.IRI;

/**
 * When a mismatch is detected between the mapping and the TBox (ontology)
 */
public class MappingOntologyMismatchException extends MappingException {

    public MappingOntologyMismatchException(IRI predicateIRI,
                                            String declaredTypeString,
                                            String usedTypeString) {
        super(predicateIRI +
                " is declared as " +
                declaredTypeString +
                " in the ontology, but is used as " +
                usedTypeString +
                " in the triplesMap:");
    }

    public MappingOntologyMismatchException(MappingOntologyMismatchException e, String message) {
        super(e.getMessage() + message, e);
    }
}
