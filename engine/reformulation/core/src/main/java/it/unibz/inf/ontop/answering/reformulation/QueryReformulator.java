package it.unibz.inf.ontop.answering.reformulation;


import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.input.InputQueryFactory;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.iq.IQ;

/**
 * See ReformulationFactory for creating a new instance.
 */
public interface QueryReformulator {

    IQ reformulateIntoNativeQuery(InputQuery inputQuery, QueryLogger queryLogger) throws OntopReformulationException;

    /**
     * For analysis purposes
     */
    String getRewritingRendering(InputQuery query) throws OntopReformulationException;

    InputQueryFactory getInputQueryFactory();

    QueryLogger.Factory getQueryLoggerFactory();
}
