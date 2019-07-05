package it.unibz.inf.ontop.iq.transform.node.impl;

import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * Does nothing
 */
public class IdentityQueryNodeTransformer implements HomogeneousQueryNodeTransformer {
    @Override
    public FilterNode transform(FilterNode filterNode) {
        return filterNode;
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return extensionalDataNode;
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return leftJoinNode;
    }

    @Override
    public UnionNode transform(UnionNode unionNode) {
        return unionNode;
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return intensionalDataNode;
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) {
        return innerJoinNode;
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) {
        return constructionNode;
    }

    @Override
    public AggregationNode transform(AggregationNode aggregationNode) {
        return aggregationNode;
    }

    @Override
    public EmptyNode transform(EmptyNode emptyNode) { return emptyNode; }

    @Override
    public TrueNode transform(TrueNode trueNode) { return trueNode; }

    @Override
    public DistinctNode transform(DistinctNode distinctNode) {
        return distinctNode;
    }

    @Override
    public SliceNode transform(SliceNode sliceNode) {
        return sliceNode;
    }

    @Override
    public OrderByNode transform(OrderByNode orderByNode) {
        return orderByNode;
    }

}

