package it.unibz.inf.ontop.rdf4j.predefined;


/**
 * Exception that may occur after the first results have been streamed
 */
public class LateEvaluationOrConversionException extends Exception {

    public LateEvaluationOrConversionException(Throwable e) {
        super(e);
    }
}
