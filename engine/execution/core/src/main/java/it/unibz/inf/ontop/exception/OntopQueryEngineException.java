package it.unibz.inf.ontop.exception;

/**
 * High-level exception produced by the query engine.
 */
public abstract class OntopQueryEngineException extends Exception {
    
    protected OntopQueryEngineException(String message) {
        super(message);
    }

    protected OntopQueryEngineException(String message, Exception e) {
        super(message, e);
    }

    protected OntopQueryEngineException(Exception e) {
        super(e);
    }
}
