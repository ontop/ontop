package it.unibz.inf.ontop.rdf4j.predefined.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedGraphQuery;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQueries;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedTupleQuery;

public class PredefinedQueriesImpl implements PredefinedQueries {

    private final ImmutableMap<String, PredefinedTupleQuery> tupleQueries;
    private final ImmutableMap<String, PredefinedGraphQuery> graphQueries;
    private final ImmutableMap<String, Object> contextMap;

    public PredefinedQueriesImpl(ImmutableMap<String, PredefinedTupleQuery> tupleQueries,
                                 ImmutableMap<String, PredefinedGraphQuery> graphQueries,
                                 ImmutableMap<String, Object> contextMap) {
        this.tupleQueries = tupleQueries;
        this.graphQueries = graphQueries;
        this.contextMap = contextMap;
    }

    @Override
    public ImmutableMap<String, PredefinedGraphQuery> getGraphQueries() {
        return graphQueries;
    }

    @Override
    public ImmutableMap<String, PredefinedTupleQuery> getTupleQueries() {
        return tupleQueries;
    }

    @Override
    public ImmutableMap<String, Object> getContextMap() {
        return contextMap;
    }
}
