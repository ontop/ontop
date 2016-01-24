package org.semanticweb.ontop.exception;

/**
 * Exceptions due to internal bugs, not due to misconfiguration from the user or from a lack of support.
 *
 * To be derived.
 */
public abstract class OntopLowLevelException extends OntopRuntimeException {

    protected OntopLowLevelException(String message) {
        super(message);
    }
}
