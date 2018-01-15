package it.unibz.inf.ontop.iq.transform;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;


/**
 *  For composite IQ trees, the tree itself is passed as a first argument,
 *  in case the transformer does not transform the tree,
 *  so as to avoid creating unnecessary new objects.
 */
public interface IQTransformer {

    IQTree transformIntensionalData(IntensionalDataNode dataNode);
    IQTree transformExtensionalData(ExtensionalDataNode dataNode);
    IQTree transformEmpty(EmptyNode node);
    IQTree transformTrue(TrueNode node);
    IQTree transformNonStandardLeafNode(LeafIQTree leafNode);

    IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child);
    IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child);
    IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child);
    IQTree transformLimit(IQTree tree, LimitNode rootNode, IQTree child);
    IQTree transformOffset(IQTree tree, OffsetNode rootNode, IQTree child);
    IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child);
    IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child);

    IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild);
    IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                        IQTree leftChild, IQTree rightChild);

    IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children);
    IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children);
    IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children);
}
