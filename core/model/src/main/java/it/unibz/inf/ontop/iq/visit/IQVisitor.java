package it.unibz.inf.ontop.iq.visit;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;


public interface IQVisitor<T> {

    T visitIntensionalData(IntensionalDataNode dataNode);
    T visitExtensionalData(ExtensionalDataNode dataNode);
    T visitEmpty(EmptyNode node);
    T visitTrue(TrueNode node);
    T visitNative(NativeNode nativeNode);
    T visitValues(ValuesNode valuesNode);
    T visitNonStandardLeafNode(LeafIQTree leafNode);

    T visitConstruction(ConstructionNode rootNode, IQTree child);
    T visitAggregation(AggregationNode aggregationNode, IQTree child);
    T visitFilter(FilterNode rootNode, IQTree child);
    T visitDistinct(DistinctNode rootNode, IQTree child);
    T visitSlice(SliceNode sliceNode, IQTree child);
    T visitOrderBy(OrderByNode rootNode, IQTree child);
    T visitNonStandardUnaryNode(UnaryOperatorNode rootNode, IQTree child);

    T visitLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild);
    T visitNonStandardBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode,
                                                        IQTree leftChild, IQTree rightChild);

    T visitInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children);
    T visitUnion(UnionNode rootNode, ImmutableList<IQTree> children);
    T visitNonStandardNaryNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children);
}
