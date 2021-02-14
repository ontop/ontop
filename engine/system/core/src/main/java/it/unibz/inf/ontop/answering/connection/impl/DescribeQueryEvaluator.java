package it.unibz.inf.ontop.answering.connection.impl;

import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.DescribeQuery;
import it.unibz.inf.ontop.answering.reformulation.input.SelectQuery;
import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryEvaluationException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;

public class DescribeQueryEvaluator {

    private final QueryReformulator engine;
    private final QueryLogger.Factory queryLoggerFactory;
    private final Evaluator<TupleResultSet, SelectQuery> selectQueryEvaluator;
    private final Evaluator<GraphResultSet, ConstructQuery> graphQueryEvaluator;

    public DescribeQueryEvaluator(QueryReformulator engine, QueryLogger.Factory queryLoggerFactory,
                                  Evaluator<TupleResultSet, SelectQuery> selectQueryEvaluator,
                                  Evaluator<GraphResultSet, ConstructQuery> constructQueryEvaluator) {

        this.engine = engine;
        this.queryLoggerFactory = queryLoggerFactory;
        this.selectQueryEvaluator = selectQueryEvaluator;
        this.graphQueryEvaluator = constructQueryEvaluator;
    }

    public GraphResultSet evaluate(DescribeQuery describeQuery, QueryLogger queryLogger)
            throws OntopQueryEvaluationException, OntopResultConversionException, OntopConnectionException,
            OntopReformulationException {

        throw new RuntimeException("TODO: implement ");
    }
}
