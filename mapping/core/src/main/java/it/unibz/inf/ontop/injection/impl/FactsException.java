package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.exception.OBDASpecificationException;

public class FactsException extends OBDASpecificationException {
    public FactsException(String message) {
        super(message);
    }

    public FactsException(Exception e) {
        super(e);
    }

    public FactsException(String message, Exception e) {
        super(message, e);
    }
}
