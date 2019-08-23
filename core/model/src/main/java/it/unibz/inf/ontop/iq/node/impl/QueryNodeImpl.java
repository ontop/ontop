package it.unibz.inf.ontop.iq.node.impl;

import it.unibz.inf.ontop.iq.node.QueryNode;


public abstract class QueryNodeImpl implements QueryNode {

    protected QueryNodeImpl() {
    }

    /**
     * See https://stackoverflow.com/questions/6837362/
     */
    @Override
    public QueryNode clone() {
        throw new UnsupportedOperationException("This method must be override. Tricks the compiler");
    }
}
