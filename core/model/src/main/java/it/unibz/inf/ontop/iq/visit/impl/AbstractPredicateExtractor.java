package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

import java.util.stream.Stream;

/**
 * Extracts data nodes (intensional or extensional)
 */
public abstract class AbstractPredicateExtractor<T extends LeafIQTree> implements IQVisitor<Stream<T>> {

    @Override
    public Stream<T> visitEmpty(EmptyNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitTrue(TrueNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitNative(NativeNode nativeNode) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitValues(ValuesNode valuesNode) {
        return Stream.empty();
    }

    @Override
    public Stream<T> visitConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> visitAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> visitFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> visitDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> visitSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> visitOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> visitFlatten(IQTree tree, FlattenNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> visitLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return Stream.of(leftChild, rightChild)
                .flatMap(c -> c.acceptVisitor(this));
    }

    @Override
    public Stream<T> visitInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }

    @Override
    public Stream<T> visitUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }
}
