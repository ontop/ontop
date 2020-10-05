package it.unibz.inf.ontop.rdf4j.predefined;

import it.unibz.inf.ontop.exception.OntopReformulationException;

public class InvalidBindingSetException extends OntopReformulationException {

    protected InvalidBindingSetException(String message) {
        super(message);
    }

    public InvalidBindingSetException(Exception e) {
        super(e);
    }

    protected InvalidBindingSetException(String message, Exception e) {
        super(message, e);
    }
}
