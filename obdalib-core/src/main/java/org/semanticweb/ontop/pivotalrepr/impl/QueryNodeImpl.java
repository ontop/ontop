package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.*;

/**
 * TODO: continue
 *
 * TODO: make it abstract
 */
public class QueryNodeImpl implements QueryNode {

    private final IntermediateQuery query;

    public QueryNodeImpl(IntermediateQuery query) {
        this.query = query;
    }

    /**
     * TODO: implement
     */
    @Override
    public QueryModifiers getModifiers() {
        return null;
    }

    /**
     * TODO: implement
     */
    @Override
    public boolean hasModifiers() {
        return false;
    }

    /**
     * TODO: implement
     */
    @Override
    public BooleanExpression getFilterExpression() {
        return null;
    }

    /**
     * TODO: implement
     */
    @Override
    public boolean hasFilterExpression() {
        return false;
    }

    /**
     * TODO: implement
     */
    @Override
    public boolean isRejected() {
        return false;
    }

    @Override
    public ImmutableList<QueryNode> getCurrentSubNodes() {
        return query.getCurrentSubNodesOf(this);
    }
}
