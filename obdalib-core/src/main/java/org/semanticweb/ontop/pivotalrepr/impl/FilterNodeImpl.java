package org.semanticweb.ontop.pivotalrepr.impl;


import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.*;

public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";

    public FilterNodeImpl(ImmutableBooleanExpression filterCondition) {
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
        return new FilterNodeImpl(getOptionalFilterCondition().get());
    }

    @Override
    public FilterNode acceptNodeTransformer(QueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableBooleanExpression getFilterCondition() {
        return getOptionalFilterCondition().get();
    }

    @Override
    public String toString() {
        return "(" + FILTER_NODE_STR + getOptionalFilterString() + ")";
    }
}
