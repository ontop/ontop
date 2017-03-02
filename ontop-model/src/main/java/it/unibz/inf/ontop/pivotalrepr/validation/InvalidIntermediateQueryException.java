package it.unibz.inf.ontop.pivotalrepr.validation;

import it.unibz.inf.ontop.exception.OntopIllegalStateException;

/**
 * Thrown by validators
 */
public class InvalidIntermediateQueryException extends OntopIllegalStateException {
    public InvalidIntermediateQueryException(String message) {
        super(message);
    }
}
