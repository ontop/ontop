package it.unibz.inf.ontop.iq.visit.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;

/**
 * A superclass for {@code IQTreeVisitor}s that transform a given {@code IQTree} into a {@code T}.
 * <p>
 * The default implementation of all visitor methods is non-recursive and delegates to {@code done()}.
 * <p>
 * To be extended by overriding the methods of interest.
 *
 * @param <T>
 */

public abstract class AbstractIQTreeGenericVisitingTransformer<T> implements IQTreeVisitor<T> {

    public final T transform(IQTree tree) {
        return tree.acceptVisitor(this);
    }

    protected abstract T done();

    @Override
    public T transformIntensionalData(IntensionalDataNode tree) {
        return done();
    }

    @Override
    public T transformExtensionalData(ExtensionalDataNode tree) {
        return done();
    }

    @Override
    public T transformEmpty(EmptyNode tree) {
        return done();
    }

    @Override
    public T transformTrue(TrueNode tree) {
        return done();
    }

    @Override
    public T transformValues(ValuesNode tree) {
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
