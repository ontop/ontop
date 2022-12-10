package it.unibz.inf.ontop.query;

public interface ConstructQuery extends GraphSPARQLQuery {


    /**
     * To be called after reformulating the query
     */
    ConstructTemplate getConstructTemplate() throws IllegalStateException;
}
