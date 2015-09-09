package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.*;


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
