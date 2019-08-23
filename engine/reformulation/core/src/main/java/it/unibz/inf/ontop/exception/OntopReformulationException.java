package it.unibz.inf.ontop.exception;


/**
 * High-level exception occuring during query reformulation
 */
public class OntopReformulationException extends OntopQueryAnsweringException {

    protected OntopReformulationException(String message) {
        super(message);
    }

    public OntopReformulationException(Exception e) {
        super(e);
    }

    protected OntopReformulationException(String message, Exception e) {
        super(message, e);
    }
}
