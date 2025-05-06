package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

public abstract class LazyRecursiveIQTreeVisitingTransformer implements IQTreeVisitingTransformer {

    protected final IntermediateQueryFactory iqFactory;

    protected LazyRecursiveIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
        return dataNode;
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
        return dataNode;
    }

    @Override
    public IQTree transformEmpty(EmptyNode node) {
        return node;
    }

    @Override
    public IQTree transformTrue(TrueNode node) {
        return node;
    }

    @Override
    public IQTree transformValues(ValuesNode node) {
        return node;
    }

    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
        return transformUnaryNode(tree, aggregationNode, child);
    }

    @Override
    public IQTree transformFlatten(UnaryIQTree tree, FlattenNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        return transformUnaryNode(tree, sliceNode, child);
    }

    @Override
    public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild);
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children);
    }

    @Override
    public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children);
    }

    private UnaryIQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        IQTree newChild = child.acceptVisitor(this);
        return (child == newChild)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    protected NaryIQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> childrenReplacement = children.stream()
                .map(t -> t.acceptVisitor(this))
                .collect(ImmutableCollectors.toList());
        return IntStream.range(0, children.size())
                .allMatch(i -> children.get(i) == childrenReplacement.get(i))
                ? tree
                : iqFactory.createNaryIQTree(rootNode, childrenReplacement);
    }

    protected BinaryNonCommutativeIQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = leftChild.acceptVisitor(this);
        IQTree newRightChild = rightChild.acceptVisitor(this);
        return (leftChild == newLeftChild) && (rightChild == newRightChild)
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }
}
