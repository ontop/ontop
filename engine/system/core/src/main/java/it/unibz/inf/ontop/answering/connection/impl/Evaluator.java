package it.unibz.inf.ontop.answering.connection.impl;

import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryEvaluationException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;

/**
 * TODO: explain
 */
@FunctionalInterface
public interface Evaluator<R extends OBDAResultSet, Q extends KGQuery<R>> {

    R evaluate(Q inputQuery, QueryLogger queryLogger)
            throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException,
            OntopReformulationException;
}
