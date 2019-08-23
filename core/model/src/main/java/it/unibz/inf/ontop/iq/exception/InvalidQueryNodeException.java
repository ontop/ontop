package it.unibz.inf.ontop.iq.exception;


import it.unibz.inf.ontop.exception.OntopInternalBugException;

public class InvalidQueryNodeException extends OntopInternalBugException {

    public InvalidQueryNodeException(String message) {
        super(message);
    }
}
