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
    public Stream<T> transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformFlatten(IQTree tree, FlattenNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<T> transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return Stream.of(leftChild, rightChild)
                .flatMap(c -> c.acceptVisitor(this));
    }

    @Override
    public Stream<T> transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }

    @Override
    public Stream<T> transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }
}
