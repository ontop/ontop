package it.unibz.inf.ontop.iq.transform.node;

import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;

/**
 * TODO: explain
 */
public interface QueryNodeTransformer {

    FilterNode transform(FilterNode filterNode, UnaryIQTree tree);

    ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode);

    LeftJoinNode transform(LeftJoinNode leftJoinNode, BinaryNonCommutativeIQTree tree);

    UnionNode transform(UnionNode unionNode, NaryIQTree tree);

    IntensionalDataNode transform(IntensionalDataNode intensionalDataNode);

    InnerJoinNode transform(InnerJoinNode innerJoinNode, NaryIQTree tree);

    ConstructionNode transform(ConstructionNode constructionNode, UnaryIQTree tree);

    AggregationNode transform(AggregationNode aggregationNode, UnaryIQTree tree);

    FlattenNode transform(FlattenNode flattenNode, UnaryIQTree tree);

    EmptyNode transform(EmptyNode emptyNode);

    TrueNode transform(TrueNode trueNode);

    ValuesNode transform(ValuesNode valuesNode);

    DistinctNode transform(DistinctNode distinctNode, UnaryIQTree tree);

    SliceNode transform(SliceNode sliceNode, UnaryIQTree tree);

    OrderByNode transform(OrderByNode orderByNode, UnaryIQTree tree);
}
