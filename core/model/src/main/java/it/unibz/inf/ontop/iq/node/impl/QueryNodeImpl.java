package it.unibz.inf.ontop.iq.node.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.QueryNode;

public abstract class QueryNodeImpl implements QueryNode {

    protected final IntermediateQueryFactory iqFactory;

    QueryNodeImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
