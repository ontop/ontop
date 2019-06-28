package it.unibz.inf.ontop.iq.transform.node;

import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.AggregationNodeImpl;

/**
 * TODO: explain
 */
public interface HeterogeneousQueryNodeTransformer<P extends NodeTransformationProposal> {

    P transform(FilterNode filterNode);

    P transform(ExtensionalDataNode extensionalDataNode);

    P transform(LeftJoinNode leftJoinNode);

    P transform(UnionNode unionNode);

    P transform(IntensionalDataNode intensionalDataNode);

    P transform(InnerJoinNode innerJoinNode);

    P transform(ConstructionNode constructionNode);

    P transform(AggregationNode aggregationNode);

    P transform(EmptyNode emptyNode);

    P transform(TrueNode trueNode);

    P transform(DistinctNode distinctNode);

    P transform(SliceNode sliceNode);

    P transform(OrderByNode orderByNode);
}
