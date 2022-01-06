package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * To be extended by overloading the methods of interest.
 */
public abstract class DefaultRecursiveIQTreeVisitingTransformer implements IQTreeVisitingTransformer {

    protected final IntermediateQueryFactory iqFactory;

    protected DefaultRecursiveIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    protected DefaultRecursiveIQTreeVisitingTransformer(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
    }

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
        IQTree newChild = child.acceptTransformer(this);
        return newChild.equals(child) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(t -> t.acceptTransformer(this))
                .collect(ImmutableCollectors.toList());

        return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createNaryIQTree(rootNode, newChildren);
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = leftChild.acceptTransformer(this);
        IQTree newRightChild = rightChild.acceptTransformer(this);
        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }
}
