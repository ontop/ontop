package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;

/**
 * To be extended by overloading the methods of interest.
 */
public abstract class DefaultNonRecursiveIQTreeExtendedTransformer<T> implements IQTreeExtendedTransformer<T> {

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode dataNode, T context) {
        return transformLeaf(dataNode, context);
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode dataNode, T context) {
        return transformLeaf(dataNode, context);
    }

    @Override
    public IQTree transformEmpty(EmptyNode node, T context) {
        return transformLeaf(node, context);
    }

    @Override
    public IQTree transformTrue(TrueNode node, T context) {
        return transformLeaf(node, context);
    }

    @Override
    public IQTree transformValues(ValuesNode node, T context) {
        return transformLeaf(node, context);
    }

    @Override
    public IQTree transformNonStandardLeafNode(LeafIQTree leafNode, T context) {
        return transformLeaf(leafNode, context);
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child, T context) {
        return transformUnaryNode(tree, rootNode, child, context);
    }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child, T context) {
        return transformUnaryNode(tree, rootNode, child, context);
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child, T context) {
        return transformUnaryNode(tree, rootNode, child, context);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child, T context) {
        return transformUnaryNode(tree, rootNode, child, context);
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child, T context) {
        return transformUnaryNode(tree, sliceNode, child, context);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child, T context) {
        return transformUnaryNode(tree, rootNode, child, context);
    }

    @Override
    public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child, T context) {
        return transformUnaryNode(tree, rootNode, child, context);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild,
                                    T context) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild, context);
    }

    @Override
    public IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                               IQTree leftChild, IQTree rightChild, T context) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild, context);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children, T context) {
        return transformNaryCommutativeNode(tree, rootNode, children, context);
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children, T context) {
        return transformNaryCommutativeNode(tree, rootNode, children, context);
    }

    @Override
    public IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children,
                                               T context) {
        return transformNaryCommutativeNode(tree, rootNode, children, context);
    }

    protected IQTree transformLeaf(LeafIQTree leaf, T context){
        return leaf;
    }

    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child, T context) {
        return tree;
    }

    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children,
                                                  T context) {
        return tree;
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild,
                                                       IQTree rightChild, T context) {
        return tree;
    }
}
