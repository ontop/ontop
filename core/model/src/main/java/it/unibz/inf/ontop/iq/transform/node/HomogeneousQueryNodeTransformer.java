package it.unibz.inf.ontop.iq.transform.node;

import it.unibz.inf.ontop.iq.node.*;

/**
 * TODO: explain
 */
public interface HomogeneousQueryNodeTransformer {

    FilterNode transform(FilterNode filterNode);

    ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode);

    LeftJoinNode transform(LeftJoinNode leftJoinNode);

    UnionNode transform(UnionNode unionNode);

    IntensionalDataNode transform(IntensionalDataNode intensionalDataNode);

    InnerJoinNode transform(InnerJoinNode innerJoinNode);

    ConstructionNode transform(ConstructionNode constructionNode);

    AggregationNode transform(AggregationNode aggregationNode);

    FlattenNode transform(FlattenNode flattenNode);

    EmptyNode transform(EmptyNode emptyNode);

    TrueNode transform(TrueNode trueNode);

    ValuesNode transform(ValuesNode valuesNode);

    DistinctNode transform(DistinctNode distinctNode);
    SliceNode transform(SliceNode sliceNode);
    OrderByNode transform(OrderByNode orderByNode);
}
