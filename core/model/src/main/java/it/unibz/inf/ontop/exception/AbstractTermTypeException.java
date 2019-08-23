package it.unibz.inf.ontop.exception;

import it.unibz.inf.ontop.model.type.TermType;


public class AbstractTermTypeException extends IncompatibleTermException {
    public AbstractTermTypeException(TermType actualTermType) {
        super("Abstract term type used for an argument: ", actualTermType);
    }
}
