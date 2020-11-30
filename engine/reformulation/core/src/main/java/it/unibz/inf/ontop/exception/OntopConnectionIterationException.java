package it.unibz.inf.ontop.exception;

import org.eclipse.rdf4j.query.QueryEvaluationException;

public class OntopConnectionIterationException extends QueryEvaluationException {

    public OntopConnectionIterationException(Exception e) {
        super(e);
    }
}
