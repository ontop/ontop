package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;

import java.util.stream.Stream;

/**
 * A superclass for {@code IQTreeVisitor} that transforms a given {@code IQTree} into a {@code Stream<T>}
 *
 * The default implementation of all visitor methods is recursive:
 * the leaves return the empty {@code Optional}, while all composite {@code IQTree}s concatenate the streams of their children.
 *
 * @param <T>
 */
public class DefaultIQTreeStreamVisitingTransformer<T> extends AbstractIQTreeGenericVisitingTransformer<Stream<T>> {

    @Override
    protected final Stream<T> done() {
        return Stream.empty();
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

    protected final Stream<T> transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
        return transform(child);
    }

    protected final Stream<T> transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode node, ImmutableList<IQTree> children) {
        return children.stream().flatMap(this::transform);
    }

    protected final Stream<T> transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode node, IQTree leftChild, IQTree rightChild) {
        return Stream.of(leftChild, rightChild).flatMap(this::transform);
    }
}
