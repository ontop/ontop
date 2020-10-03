package it.unibz.inf.ontop.rdf4j.predefined;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.rdf4j.predefined.impl.PredefinedQueriesImpl;

public interface PredefinedQueries {

    ImmutableMap<String, PredefinedGraphQuery> getGraphQueries();
    ImmutableMap<String, PredefinedTupleQuery> getTupleQueries();

    static PredefinedQueries defaultPredefinedQueries(ImmutableMap<String, PredefinedTupleQuery> tupleQueries,
                                                      ImmutableMap<String, PredefinedGraphQuery> graphQueries) {
        return new PredefinedQueriesImpl(tupleQueries, graphQueries);
    }

}
