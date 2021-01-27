package it.unibz.inf.ontop.answering.reformulation.input;

import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;

public interface GraphSPARQLQuery extends SPARQLQuery<SimpleGraphResultSet> {

    /**
     * To be called after reformulating the query
     */
    ConstructTemplate getConstructTemplate() throws IllegalStateException;
}
