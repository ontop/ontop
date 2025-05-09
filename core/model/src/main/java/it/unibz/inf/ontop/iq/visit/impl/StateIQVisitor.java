package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.iq.visit.NormalizationState;

import java.util.Optional;

public abstract class StateIQVisitor<T extends IQVisitor<T> & NormalizationState<T>> implements IQVisitor<T>, NormalizationState<T> {

    protected final T continueTo(IQTree next) {
        return next.acceptVisitor(this);
    }

    public abstract T reduce();

    protected final T done() {
        return (T)this;
    }

    @Override
    public Optional<T> next() {
        T r = reduce();
        return r.equals(this)
                ? Optional.empty()
                : Optional.of(r);
    }

    public abstract IQTree toIQTree();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public T transformIntensionalData(IntensionalDataNode dataNode) {
        return done();
    }

    @Override
    public T transformExtensionalData(ExtensionalDataNode dataNode) {
        return done();
    }

    @Override
    public T transformEmpty(EmptyNode node) {
        return done();
    }

    @Override
    public T transformTrue(TrueNode node) {
        return done();
    }

    @Override
    public T transformNative(NativeNode nativeNode) {
        return done();
    }

    @Override
    public T transformValues(ValuesNode valuesNode) {
        return done();
    }

    @Override
    public T transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public T transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
        return done();
    }

    @Override
    public T transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public T transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public T transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public T transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        return done();
    }

    @Override
    public T transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public T transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return done();
    }

    @Override
    public T transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return done();
    }

    @Override
    public T transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return done();
    }
}
