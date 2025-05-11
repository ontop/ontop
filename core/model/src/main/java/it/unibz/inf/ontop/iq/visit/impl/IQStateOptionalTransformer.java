package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

import java.util.Optional;
import java.util.function.Function;

public abstract class IQStateOptionalTransformer<T> implements IQVisitor<Optional<T>> {

    public static <T> T reachFinalState(T initial, Function<T, Optional<? extends T>> transformer) {
        //Non-final
        T state = initial;
        while (true) {
            Optional<? extends T> next = transformer.apply(state);
            if (next.isEmpty())
                return state;
            state = next.get();
        }
    }

    public static <T> T reachFixedPoint(T initial, Function<T, T> transformer, int maxIterations) {
        //Non-final
        T state = initial;
        for(int i = 0; i < maxIterations; i++) {
            T next = transformer.apply(state);
            if (next.equals(state))
                return state;
            state = next;
        }
        throw new MinorOntopInternalBugException(String.format("Has not converged in %d iterations", maxIterations));
    }

    public static <T> T reachFinalState(T initial, Function<T, Optional<? extends T>> transformer, int maxIterations) {
        //Non-final
        T state = initial;
        for (int i = 0; i < maxIterations; i++) {
            Optional<? extends T> next = transformer.apply(state);
            if (next.isEmpty())
                return state;
            state = next.get();
        }
        throw new MinorOntopInternalBugException(String.format("Has not converged in %d iterations", maxIterations));
    }


    protected final Optional<T> done() {
        return Optional.empty();
    }

    @Override
    public Optional<T> transformIntensionalData(IntensionalDataNode tree) {
        return done();
    }

    @Override
    public Optional<T> transformExtensionalData(ExtensionalDataNode tree) {
        return done();
    }

    @Override
    public Optional<T> transformEmpty(EmptyNode tree) {
        return done();
    }

    @Override
    public Optional<T> transformTrue(TrueNode tree) {
        return done();
    }

    @Override
    public Optional<T> transformNative(NativeNode tree) {
        return done();
    }

    @Override
    public Optional<T> transformValues(ValuesNode tree) {
        return done();
    }

    @Override
    public Optional<T> transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public Optional<T> transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
        return done();
    }

    @Override
    public Optional<T> transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public Optional<T> transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public Optional<T> transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public Optional<T> transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        return done();
    }

    @Override
    public Optional<T> transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return done();
    }

    @Override
    public Optional<T> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return done();
    }

    @Override
    public Optional<T> transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return done();
    }

    @Override
    public Optional<T> transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return done();
    }

}
