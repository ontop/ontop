package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;

import java.util.stream.Stream;

public class DefaultIQTreeToStreamVisitingTransformer<T> extends AbstractIQVisitor<Stream<T>> {

    @Override
    public Stream<T> transformIntensionalData(IntensionalDataNode node) {
        return transformLeaf(node);
    }

    @Override
    public Stream<T> transformExtensionalData(ExtensionalDataNode node) {
        return transformLeaf(node);
    }

    @Override
    public Stream<T> transformEmpty(EmptyNode node) {
        return transformLeaf(node);
    }

    @Override
    public Stream<T> transformTrue(TrueNode node) {
        return transformLeaf(node);
    }

    @Override
    public Stream<T> transformNative(NativeNode node) {
        return transformLeaf(node);
    }

    @Override
    public Stream<T> transformValues(ValuesNode node) {
        return transformLeaf(node);
    }

    @Override
    public Stream<T> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public Stream<T> transformAggregation(UnaryIQTree tree, AggregationNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public Stream<T> transformFilter(UnaryIQTree tree, FilterNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public Stream<T> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public Stream<T> transformSlice(UnaryIQTree tree, SliceNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public Stream<T> transformOrderBy(UnaryIQTree tree, OrderByNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public Stream<T> transformFlatten(UnaryIQTree tree, FlattenNode node, IQTree child) {
        return transformUnaryNode(tree, node, child);
    }

    @Override
    public Stream<T> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode node, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, node, leftChild, rightChild);
    }

    @Override
    public Stream<T> transformInnerJoin(NaryIQTree tree, InnerJoinNode node, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, node, children);
    }

    @Override
    public Stream<T> transformUnion(NaryIQTree tree, UnionNode node, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, node, children);
    }

    protected Stream<T> transformLeaf(LeafIQTree leaf) {
        return Stream.of();
    }

    protected Stream<T> transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
        return transformChild(child);
    }

    protected Stream<T> transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode node, ImmutableList<IQTree> children) {
        return children.stream().flatMap(this::transformChild);
    }

    protected Stream<T> transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode node, IQTree leftChild, IQTree rightChild) {
        return Stream.of(leftChild, rightChild).flatMap(this::transformChild);
    }
}
