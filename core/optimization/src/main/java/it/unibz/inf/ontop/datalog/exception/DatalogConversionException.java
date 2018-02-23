package it.unibz.inf.ontop.datalog.exception;

import it.unibz.inf.ontop.exception.OntopInternalBugException;

/**
 * Use for IntermediateQuery/Datalog conversion, in both directions
 */
public class DatalogConversionException extends OntopInternalBugException{
    public DatalogConversionException(String message) {
        super(message);
    }
}
