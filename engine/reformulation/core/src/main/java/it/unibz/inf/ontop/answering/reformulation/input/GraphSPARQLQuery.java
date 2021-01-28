package it.unibz.inf.ontop.answering.reformulation.input;

import it.unibz.inf.ontop.answering.resultset.GraphResultSet;

public interface GraphSPARQLQuery extends SPARQLQuery<GraphResultSet> {

    /**
     * To be called after reformulating the query
     */
    ConstructTemplate getConstructTemplate() throws IllegalStateException;
}
