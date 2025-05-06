package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

import java.util.stream.Stream;

/**
 * Extracts data nodes (intensional or extensional)
 */
public abstract class AbstractPredicateExtractor<T extends LeafIQTree> implements IQVisitor<Stream<T>> {

    @Override
    public Stream<T> transformEmpty(EmptyNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<T> transformTrue(TrueNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<T> transformNative(NativeNode nativeNode) {
        return Stream.empty();
    }

    @Override
    public Stream<T> transformValues(ValuesNode valuesNode) {
        return Stream.empty();
    }

    @Override
    public Stream<T> transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return Stream.of(leftChild, rightChild)
                .flatMap(c -> c.acceptVisitor(this));
    }

    @Override
    public Stream<T> transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }

    @Override
    public Stream<T> transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }
}
