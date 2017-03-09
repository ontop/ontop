package it.unibz.inf.ontop.pivotalrepr.validation;

import it.unibz.inf.ontop.exception.OntopInternalBugException;

/**
 * Thrown by validators
 */
public class InvalidIntermediateQueryException extends OntopInternalBugException {
    public InvalidIntermediateQueryException(String message) {
        super(message);
    }
}
