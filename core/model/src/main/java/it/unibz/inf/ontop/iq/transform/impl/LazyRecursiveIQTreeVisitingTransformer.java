package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;

public abstract class LazyRecursiveIQTreeVisitingTransformer implements IQTreeVisitingTransformer {

    protected final IntermediateQueryFactory iqFactory;

    protected LazyRecursiveIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode dataNode) { return dataNode; }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode dataNode) { return dataNode; }

    @Override
    public IQTree transformEmpty(EmptyNode node) { return node; }

    @Override
    public IQTree transformTrue(TrueNode node) {
        return node;
    }

    @Override
    public IQTree transformValues(ValuesNode node) {
        return node;
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) { return transformUnaryNode(tree, rootNode, child); }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child) {
        return transformUnaryNode(tree, aggregationNode, child);
    }

    @Override
    public IQTree transformFlatten(IQTree tree, FlattenNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) { return transformUnaryNode(tree, rootNode, child); }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) { return transformUnaryNode(tree, rootNode, child); }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) { return transformUnaryNode(tree, sliceNode, child); }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) { return transformUnaryNode(tree, rootNode, child); }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) { return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild); }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) { return transformNaryCommutativeNode(tree, rootNode, children); }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) { return transformNaryCommutativeNode(tree, rootNode, children); }

    private IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        IQTree newChild = child.acceptVisitor(this);
        return (child == newChild)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    protected ImmutableList<Map.Entry<IQTree, IQTree>> transformChildren(ImmutableList<IQTree> children) {
        return children.stream()
                .map(t -> Maps.immutableEntry(t, t.acceptVisitor(this)))
                .collect(ImmutableCollectors.toList());
    }

    protected ImmutableList<IQTree> extractChildren(ImmutableList<Map.Entry<IQTree, IQTree>> newChildren) {
        return newChildren.stream()
                .map(Map.Entry::getValue)
                .collect(ImmutableCollectors.toList());
    }


    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<Map.Entry<IQTree, IQTree>> childrenReplacement = transformChildren(children);
        return childrenReplacement.stream().allMatch(e -> e.getKey() == e.getValue())
                ? tree
                : iqFactory.createNaryIQTree(rootNode, extractChildren(childrenReplacement));
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = leftChild.acceptVisitor(this);
        IQTree newRightChild = rightChild.acceptVisitor(this);
        return (leftChild == newLeftChild) && (rightChild == newRightChild)
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }
}
