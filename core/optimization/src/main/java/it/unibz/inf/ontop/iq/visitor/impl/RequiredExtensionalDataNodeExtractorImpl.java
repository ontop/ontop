package it.unibz.inf.ontop.iq.visitor.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
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
    public Stream<ExtensionalDataNode> visitIntensionalData(IntensionalDataNode dataNode) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> visitExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }

    @Override
    public Stream<ExtensionalDataNode> visitEmpty(EmptyNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> visitTrue(TrueNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> visitNative(NativeNode nativeNode) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> visitValues(ValuesNode valuesNode) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> visitConstruction(ConstructionNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    /**
     * Blocks
     */
    @Override
    public Stream<ExtensionalDataNode> visitAggregation(AggregationNode aggregationNode, IQTree child) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> visitFilter(FilterNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> visitFlatten(FlattenNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> visitDistinct(DistinctNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> visitSlice(SliceNode sliceNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> visitOrderBy(OrderByNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    /**
     * Only considers the left child
     */
    @Override
    public Stream<ExtensionalDataNode> visitLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return leftChild.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> visitInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }

    /**
     * TODO: try to extract some common data nodes
     */
    @Override
    public Stream<ExtensionalDataNode> visitUnion(UnionNode rootNode, ImmutableList<IQTree> children) {
        return Stream.empty();
    }
}
