package it.unibz.inf.ontop.iq.transform;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;

public interface IQTransformer {

    IQTree transformIntensionalData(IntensionalDataNode dataNode);
    IQTree transformExtensionalData(ExtensionalDataNode dataNode);
    IQTree transformEmpty(EmptyNode node);
    IQTree transformTrue(TrueNode node);
    IQTree transformNonStandardLeafNode(LeafIQTree leafNode);

    IQTree transformConstruction(ConstructionNode rootNode, IQTree child);
    IQTree transformFilter(FilterNode rootNode, IQTree child);
    IQTree transformNonStandardUnaryNode(UnaryOperatorNode rootNode, IQTree child);

    IQTree transformLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild);
    IQTree transformNonStandardBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode,
                                                        IQTree leftChild, IQTree rightChild);

    IQTree transformInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children);
    IQTree transformUnion(UnionNode rootNode, ImmutableList<IQTree> children);
    IQTree transformNonStandardNaryNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children);
}
