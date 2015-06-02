package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.FilterNode;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;

public abstract class FilterNodeImpl extends QueryNodeImpl implements FilterNode {

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
}
