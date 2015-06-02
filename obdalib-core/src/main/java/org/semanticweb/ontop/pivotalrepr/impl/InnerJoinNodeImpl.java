package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.*;

public class InnerJoinNodeImpl extends AbstractJoinNodeImpl implements InnerJoinNode {

    /**
     * Without condition
     */
    public InnerJoinNodeImpl() {
        super();
    }

    protected InnerJoinNodeImpl(BooleanExpression joinCondition) {
        super(joinCondition);
    }


    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }
}
