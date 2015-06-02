package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
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
}
