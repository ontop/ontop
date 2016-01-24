package org.semanticweb.ontop.exception;

/**
 * Ancestor of all the Ontop-specific RuntimeException
 */
public abstract class OntopRuntimeException extends RuntimeException {

    protected OntopRuntimeException(String message) {
        super(message);
    }
}
