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
 * Delegates the call to transformLeaf, transformUnaryNode,
 * transformNaryCommutativeNode or transformBinaryNonCommutativeNode
 * depending on the type of the node.
 *
 * Method transformLeaf simply returns the tree unchanged.
 * Method transformUnaryNode, transformNaryCommutativeNode
 * or transformBinaryNonCommutativeNode apply the transformer to
 * their children and if the result is different, creates a new subtree;
 * otherwise, the input tree is reused.
 *
 * To be extended by overloading the methods of interest.
 */
public abstract class DefaultRecursiveIQTreeVisitingTransformer implements IQTreeVisitingTransformer {

    protected final IntermediateQueryFactory iqFactory;

    protected DefaultRecursiveIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
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
    public IQTree transformFlatten(IQTree tree, FlattenNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
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

    protected final ImmutableList<IQTree> transformChildren(ImmutableList<IQTree> children) {
        return children.stream()
                .map(this::transformChild)
                .collect(ImmutableCollectors.toList());
    }

    protected final IQTree transformChild(IQTree child) {
        return child.acceptVisitor(this);
    }

    protected IQTree transformLeaf(LeafIQTree leaf){
        return leaf;
    }

    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        IQTree newChild = transformChild(child);
        return newChild.equals(child) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = transformChildren(children);
        return newChildren.equals(children) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createNaryIQTree(rootNode, newChildren);
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = transformChild(leftChild);
        IQTree newRightChild = transformChild(rightChild);
        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild) && rootNode.equals(tree.getRootNode())
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }
}
