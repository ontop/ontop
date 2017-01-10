package it.unibz.inf.ontop.exception;

/**
 * High-level exception: should bring a clear message to the user.
 */
public abstract class OntopHighLevelException extends OntopRuntimeException {

    protected OntopHighLevelException(String message) {
        super(message);
    }
}
