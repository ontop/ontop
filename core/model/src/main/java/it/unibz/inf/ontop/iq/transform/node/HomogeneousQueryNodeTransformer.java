package it.unibz.inf.ontop.iq.transform.node;

import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;

/**
 * TODO: explain
 */
public interface HomogeneousQueryNodeTransformer<T1 extends QueryNodeTransformationException,
        T2 extends QueryNodeTransformationException> {

    FilterNode transform(FilterNode filterNode) throws T1, T2;

    ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) throws T1, T2;

    LeftJoinNode transform(LeftJoinNode leftJoinNode) throws T1, T2;

    UnionNode transform(UnionNode unionNode) throws T1, T2;

    IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) throws T1, T2;

    InnerJoinNode transform(InnerJoinNode innerJoinNode) throws T1, T2;

    ConstructionNode transform(ConstructionNode constructionNode) throws T1, T2;

    AggregationNode transform(AggregationNode aggregationNode) throws T1, T2;

    EmptyNode transform(EmptyNode emptyNode);

    TrueNode transform(TrueNode trueNode);

    ValuesNode transform(ValuesNode valuesNode) throws T1, T2;

    DistinctNode transform(DistinctNode distinctNode) throws T1, T2;
    SliceNode transform(SliceNode sliceNode) throws T1, T2;
    OrderByNode transform(OrderByNode orderByNode) throws T1, T2;
}
