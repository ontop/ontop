package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.*;

public class InnerJoinNodeImpl extends AbstractJoinNodeImpl implements InnerJoinNode {

    public InnerJoinNodeImpl(Optional<BooleanExpression> optionalFilterCondition) {
        super(optionalFilterCondition);
    }


    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public InnerJoinNode clone() {
        Optional<BooleanExpression> originalOptionalFilter = getOptionalFilterCondition();
        Optional<BooleanExpression> newOptionalFilter;
        if (originalOptionalFilter.isPresent()) {
            // Not yet safe --> must be cloned. TODO: make it immutable
            BooleanExpression newFilter = originalOptionalFilter.get().clone();
            newOptionalFilter = Optional.of(newFilter);
        }
        else{
            newOptionalFilter = Optional.absent();
        }

        return new InnerJoinNodeImpl(newOptionalFilter);
    }
}
