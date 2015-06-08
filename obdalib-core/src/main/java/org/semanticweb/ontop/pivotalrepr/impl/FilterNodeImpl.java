package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.FilterNode;

public abstract class FilterNodeImpl extends QueryNodeImpl implements FilterNode {

    private Optional<BooleanExpression> optionalFilterCondition;

    protected FilterNodeImpl(Optional<BooleanExpression> optionalFilterCondition) {
        this.optionalFilterCondition = optionalFilterCondition;
    }

    @Override
    public Optional<BooleanExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }
}
