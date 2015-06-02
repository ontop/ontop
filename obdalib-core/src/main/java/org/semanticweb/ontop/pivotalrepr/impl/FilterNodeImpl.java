package org.semanticweb.ontop.pivotalrepr.impl;


import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.FilterNode;

public abstract class FilterNodeImpl extends QueryNodeImpl implements FilterNode {

    private BooleanExpression filterCondition;

    protected FilterNodeImpl() {
        filterCondition = null;
    }

    protected FilterNodeImpl(BooleanExpression filterCondition) {
        this.filterCondition = filterCondition;
    }

    @Override
    public BooleanExpression getFilterCondition() {
        return filterCondition;
    }

    @Override
    public boolean hasFilterCondition() {
        return filterCondition != null;
    }
}
