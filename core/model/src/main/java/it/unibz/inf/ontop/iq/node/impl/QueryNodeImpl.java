package it.unibz.inf.ontop.iq.node.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.QueryNode;

public abstract class QueryNodeImpl implements QueryNode {

    protected final IntermediateQueryFactory iqFactory;

    QueryNodeImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("hashCode() must be overridden");
    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("equals() must be overridden");
    }
}
