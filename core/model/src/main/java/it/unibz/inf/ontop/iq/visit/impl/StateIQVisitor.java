package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

import java.util.function.Supplier;

public abstract class StateIQVisitor<T extends IQVisitor<T>> implements IQVisitor<T> {

    private final Supplier<IQTree> nextInputSupplier;

    protected StateIQVisitor(Supplier<IQTree> nextInputSupplier) {
        this.nextInputSupplier = nextInputSupplier;
    }

    public final T next() {
        return nextInputSupplier.get().acceptVisitor(this);
    }

    protected final T stop() {
        return (T)this;
    }

    public abstract T simplify();

    public abstract IQTree toIQTree();

    @Override
    public abstract boolean equals(Object o);

    @Override
    public T transformIntensionalData(IntensionalDataNode dataNode) {
        return stop();
    }

    @Override
    public T transformExtensionalData(ExtensionalDataNode dataNode) {
        return stop();
    }

    @Override
    public T transformEmpty(EmptyNode node) {
        return stop();
    }

    @Override
    public T transformTrue(TrueNode node) {
        return stop();
    }

    @Override
    public T transformNative(NativeNode nativeNode) {
        return stop();
    }

    @Override
    public T transformValues(ValuesNode valuesNode) {
        return stop();
    }

    @Override
    public T transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return stop();
    }

    @Override
    public T transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
        return stop();
    }

    @Override
    public T transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        return stop();
    }

    @Override
    public T transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
        return stop();
    }

    @Override
    public T transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return stop();
    }

    @Override
    public T transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        return stop();
    }

    @Override
    public T transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return stop();
    }

    @Override
    public T transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return stop();
    }

    @Override
    public T transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return stop();
    }

    @Override
    public T transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return stop();
    }
}
