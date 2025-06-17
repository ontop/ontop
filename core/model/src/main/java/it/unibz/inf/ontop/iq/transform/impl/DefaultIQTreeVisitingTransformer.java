package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.impl.AbstractIQVisitor;


/**
 * Delegates the call to transformLeaf, transformUnaryNode,
 * transformNaryCommutativeNode or transformBinaryNonCommutativeNode
 * depending on the type of the node.
 *
 * To be extended by overriding the methods of interest.
 */

public abstract class DefaultIQTreeVisitingTransformer extends AbstractIQVisitor<IQTree> {

    @Override
    public final IQTree transformNative(NativeNode nativeNode) {
        throw new UnsupportedOperationException("NativeNode does not support transformer (too late)");
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode node) {
        return transformLeaf(node);
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode node) {
        return transformLeaf(node);
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
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformFilter(UnaryIQTree tree, FilterNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformSlice(UnaryIQTree tree, SliceNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformFlatten(UnaryIQTree tree, FlattenNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, node, leftChild, rightChild);
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, node, children);
    }

    @Override
    public IQTree transformUnion(NaryIQTree tree, UnionNode node, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, node, children);
    }

    protected abstract IQTree transformLeaf(LeafIQTree leaf);

    protected abstract IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child);

    protected abstract IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode node, ImmutableList<IQTree> children);

    protected abstract IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode node, IQTree leftChild, IQTree rightChild);
}
