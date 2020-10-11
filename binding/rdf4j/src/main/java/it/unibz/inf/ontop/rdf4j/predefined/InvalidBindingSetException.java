package it.unibz.inf.ontop.rdf4j.predefined;

import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;

public class InvalidBindingSetException extends OntopQueryAnsweringException {

    public InvalidBindingSetException(String message) {
        super(message);
    }

    public InvalidBindingSetException(Exception e) {
        super(e);
    }

    public InvalidBindingSetException(String message, Exception e) {
        super(message, e);
    }
}
