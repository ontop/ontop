package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;
import org.semanticweb.ontop.pivotalrepr.SimpleFilterNode;

public class SimpleFilterNodeImpl extends FilterNodeImpl implements SimpleFilterNode {

    public SimpleFilterNodeImpl(BooleanExpression filterCondition) {
        super(filterCondition);
    }

    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }
}
