package it.unibz.inf.ontop.iq.visitor.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;


@Singleton
public class RequiredExtensionalDataNodeExtractorImpl implements RequiredExtensionalDataNodeExtractor {

    @Inject
    protected RequiredExtensionalDataNodeExtractorImpl() {
    }

    @Override
    public Stream<ExtensionalDataNode> transformIntensionalData(IntensionalDataNode dataNode) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }

    @Override
    public Stream<ExtensionalDataNode> transformEmpty(EmptyNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformTrue(TrueNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformNative(NativeNode nativeNode) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformValues(ValuesNode valuesNode) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    /**
     * Blocks
     */
    @Override
    public Stream<ExtensionalDataNode> transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformFlatten(IQTree tree, FlattenNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    /**
     * Only considers the left child
     */
    @Override
    public Stream<ExtensionalDataNode> transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return leftChild.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }

    /**
     * TODO: try to extract some common data nodes
     */
    @Override
    public Stream<ExtensionalDataNode> transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return Stream.empty();
    }
}
