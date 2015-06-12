package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryNodeVisitor;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;
import org.semanticweb.ontop.pivotalrepr.SimpleFilterNode;

public class SimpleFilterNodeImpl extends FilterNodeImpl implements SimpleFilterNode {

    public SimpleFilterNodeImpl(BooleanExpression filterCondition) {
        super(Optional.of(filterCondition));
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
    public SimpleFilterNode clone() {
        BooleanExpression filterCondition = getOptionalFilterCondition().get();

        return new SimpleFilterNodeImpl(filterCondition.clone());
    }
}
