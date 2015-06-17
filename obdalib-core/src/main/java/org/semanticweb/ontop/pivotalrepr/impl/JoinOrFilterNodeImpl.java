package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.JoinOrFilterNode;

public abstract class JoinOrFilterNodeImpl extends QueryNodeImpl implements JoinOrFilterNode {

    private Optional<BooleanExpression> optionalFilterCondition;

    protected JoinOrFilterNodeImpl(Optional<BooleanExpression> optionalFilterCondition) {
        this.optionalFilterCondition = optionalFilterCondition;
    }

    @Override
    public Optional<BooleanExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }
}
