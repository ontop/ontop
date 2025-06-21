package it.unibz.inf.ontop.iq.transform.node;

import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;

/**
 * Works with {@link it.unibz.inf.ontop.iq.transform.impl.IQTreeVisitingNodeTransformer},
 * which traverses the tree recursively and applies the node transformer to each subtree.
 * Note that it creates a new composite subtree only if the node is different (as .equals) or
 * if the child is different (as == rather than .equals because the transformer changes only nodes).
 *
 * The tree argument can be used for getting additional context information for transforming the node.
 *
 * @see DefaultQueryNodeTransformer
 */

public interface QueryNodeTransformer {

    ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode);
    IntensionalDataNode transform(IntensionalDataNode intensionalDataNode);
    EmptyNode transform(EmptyNode emptyNode);
    TrueNode transform(TrueNode trueNode);
    ValuesNode transform(ValuesNode valuesNode);

    DistinctNode transform(DistinctNode distinctNode, UnaryIQTree tree);
    SliceNode transform(SliceNode sliceNode, UnaryIQTree tree);
    OrderByNode transform(OrderByNode orderByNode, UnaryIQTree tree);
    FilterNode transform(FilterNode filterNode, UnaryIQTree tree);
    FlattenNode transform(FlattenNode flattenNode, UnaryIQTree tree);
    ConstructionNode transform(ConstructionNode constructionNode, UnaryIQTree tree);
    AggregationNode transform(AggregationNode aggregationNode, UnaryIQTree tree);

    LeftJoinNode transform(LeftJoinNode leftJoinNode, BinaryNonCommutativeIQTree tree);

    UnionNode transform(UnionNode unionNode, NaryIQTree tree);
    InnerJoinNode transform(InnerJoinNode innerJoinNode, NaryIQTree tree);
}
