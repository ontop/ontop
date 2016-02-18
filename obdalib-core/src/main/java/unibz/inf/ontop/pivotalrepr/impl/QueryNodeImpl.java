package unibz.inf.ontop.pivotalrepr.impl;

import unibz.inf.ontop.pivotalrepr.QueryNode;


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
