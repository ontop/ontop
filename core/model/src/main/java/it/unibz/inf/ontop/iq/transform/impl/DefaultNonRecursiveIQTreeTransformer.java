package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;

/**
 * To be extended by overloading the methods of interest.
 */
public abstract class DefaultNonRecursiveIQTreeTransformer implements IQTreeVisitingTransformer {

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
        return transformLeaf(dataNode);
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
        return transformLeaf(dataNode);
    }

    @Override
    public IQTree transformEmpty(EmptyNode node) {
        return transformLeaf(node);
    }

    @Override
    public IQTree transformTrue(TrueNode node) {
        return transformLeaf(node);
    }

    @Override
    public IQTree transformValues(ValuesNode node) {
        return transformLeaf(node);
    }

    @Override
    public IQTree transformNonStandardLeafNode(LeafIQTree leafNode) {
        return transformLeaf(leafNode);
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        return transformUnaryNode(tree, sliceNode, child);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild);
    }

    @Override
    public IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children);
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children);
    }

    @Override
    public IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children);
    }

    protected IQTree transformLeaf(LeafIQTree leaf){
        return leaf;
    }

    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return tree;
    }

    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return tree;
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        return tree;
    }
}
