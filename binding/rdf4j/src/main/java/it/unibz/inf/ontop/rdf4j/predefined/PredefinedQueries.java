package it.unibz.inf.ontop.rdf4j.predefined;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.rdf4j.predefined.impl.PredefinedQueriesImpl;

public interface PredefinedQueries {

    ImmutableMap<String, PredefinedGraphQuery> getGraphQueries();
    ImmutableMap<String, PredefinedTupleQuery> getTupleQueries();
    ImmutableMap<String, Object> getContextMap();

    static PredefinedQueries defaultPredefinedQueries(ImmutableMap<String, PredefinedTupleQuery> tupleQueries,
                                                      ImmutableMap<String, PredefinedGraphQuery> graphQueries,
                                                      ImmutableMap<String, Object> contextMap) {
        return new PredefinedQueriesImpl(tupleQueries, graphQueries, contextMap);
    }
}
