package it.unibz.inf.ontop.iq.node.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.LimitNode;
import it.unibz.inf.ontop.iq.node.NodeTransformationProposal;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.QueryNodeVisitor;
import it.unibz.inf.ontop.iq.transform.IQTransformer;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;


public class LimitNodeImpl extends LimitOrOffsetNodeImpl implements LimitNode {

    private static final String LIMIT_NODE_STR = "LIMIT";

    private final long limit;

    @AssistedInject
    private LimitNodeImpl(@Assisted long limit, IntermediateQueryFactory iqFactory) {
        super(iqFactory);
        this.limit = limit;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTransformer transformer, IQTree child) {
        return transformer.transformLimit(tree, this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public LimitNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof LimitNode)
                && ((LimitNode) node).getLimit() == limit;
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return isSyntacticallyEquivalentTo(queryNode);
    }

    @Override
    public LimitNode clone() {
        return iqFactory.createLimitNode(limit);
    }

    @Override
    public String toString() {
        return LIMIT_NODE_STR + " " + limit;
    }
}
