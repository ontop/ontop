package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.model.BooleanExpression;
import org.semanticweb.ontop.pivotalrepr.LocalOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.QueryNodeVisitor;
import org.semanticweb.ontop.pivotalrepr.QueryOptimizer;
import org.semanticweb.ontop.pivotalrepr.FilterNode;

public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    public FilterNodeImpl(BooleanExpression filterCondition) {
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
    public FilterNode clone() {
        BooleanExpression filterCondition = getOptionalFilterCondition().get();

        return new FilterNodeImpl(filterCondition.clone());
    }
}
