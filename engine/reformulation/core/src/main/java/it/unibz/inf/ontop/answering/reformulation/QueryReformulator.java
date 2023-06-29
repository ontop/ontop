package it.unibz.inf.ontop.answering.reformulation;


import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.iq.IQ;

/**
 * See ReformulationFactory for creating a new instance.
 */
public interface QueryReformulator {

    IQ reformulateIntoNativeQuery(KGQuery<?> inputQuery, QueryLogger queryLogger) throws OntopReformulationException;

    /**
     * For analysis purposes
     */
    String getRewritingRendering(KGQuery<?> query) throws OntopReformulationException;

    KGQueryFactory getInputQueryFactory();

    QueryLogger.Factory getQueryLoggerFactory();
}
