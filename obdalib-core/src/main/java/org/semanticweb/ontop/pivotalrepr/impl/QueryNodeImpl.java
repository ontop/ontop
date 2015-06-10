package org.semanticweb.ontop.pivotalrepr.impl;

import org.semanticweb.ontop.pivotalrepr.*;

/**
 * TODO: continue
 *
 */
public abstract class QueryNodeImpl implements QueryNode {

    private boolean isRejected;

    protected QueryNodeImpl() {
        isRejected = false;
    }

    protected void declareRejection() {
        isRejected = true;
    }

    @Override
    public boolean isRejected() {
        return isRejected;
    }

    /**
     * See https://stackoverflow.com/questions/6837362/
     */
    @Override
    public QueryNode clone() {
        throw new RuntimeException("This method must be override. Tricks the compiler");
    }
}
