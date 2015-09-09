package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.JoinOrFilterNode;

public abstract class JoinOrFilterNodeImpl extends QueryNodeImpl implements JoinOrFilterNode {

    private Optional<ImmutableBooleanExpression> optionalFilterCondition;

    protected JoinOrFilterNodeImpl(Optional<ImmutableBooleanExpression> optionalFilterCondition) {
        this.optionalFilterCondition = optionalFilterCondition;
    }

    @Override
    public Optional<ImmutableBooleanExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }

    protected String getOptionalFilterString() {
        if (optionalFilterCondition.isPresent()) {
            return " " + optionalFilterCondition.get().toString();
        }

        return "";
    }
}
