package it.unibz.inf.ontop.answering.logging.impl;

import it.unibz.inf.ontop.answering.logging.QueryReformulationLogger;

import java.util.UUID;

public class QueryReformulationLoggerImpl extends AbstractQueryLogger implements QueryReformulationLogger {

    public QueryReformulationLoggerImpl(UUID queryId) {
        super(queryId, System.out);
    }
}
