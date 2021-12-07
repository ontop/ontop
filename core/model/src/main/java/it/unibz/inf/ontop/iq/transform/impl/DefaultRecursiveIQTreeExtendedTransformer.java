package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * To be extended by overloading the methods of interest.
 */
public abstract class DefaultRecursiveIQTreeExtendedTransformer<T> implements IQTreeExtendedTransformer<T> {

    protected final IntermediateQueryFactory iqFactory;

    protected DefaultRecursiveIQTreeExtendedTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    protected DefaultRecursiveIQTreeExtendedTransformer(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
    }

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
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild, T context) {
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
        IQTree newChild = child.acceptTransformer(this, context);
        return newChild.equals(child) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children,
                                                  T context) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(t -> t.acceptTransformer(this, context))
                .collect(ImmutableCollectors.toList());

        return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createNaryIQTree(rootNode, newChildren);
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild, T context) {
        IQTree newLeftChild = leftChild.acceptTransformer(this, context);
        IQTree newRightChild = rightChild.acceptTransformer(this, context);
        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }
}
