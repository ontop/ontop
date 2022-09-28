package it.unibz.inf.ontop.answering.reformulation.input;

public interface ConstructQuery extends GraphSPARQLQuery {


    /**
     * To be called after reformulating the query
     */
    ConstructTemplate getConstructTemplate() throws IllegalStateException;
}
