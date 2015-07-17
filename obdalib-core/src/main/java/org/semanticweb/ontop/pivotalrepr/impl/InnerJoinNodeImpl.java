package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.LocalOptimizationProposal;

public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;

    public InnerJoinNodeImpl(Optional<ImmutableBooleanExpression> optionalFilterCondition) {
        super(optionalFilterCondition);
    }


    @Override
    public Optional<LocalOptimizationProposal> acceptOptimizer(QueryNodeOptimizer optimizer) {
        return optimizer.makeProposal(this);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public InnerJoinNode clone() {
        return new InnerJoinNodeImpl(getOptionalFilterCondition());
    }

    @Override
    public InnerJoinNode acceptNodeTransformer(QueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }
}
