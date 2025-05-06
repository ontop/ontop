package it.unibz.inf.ontop.iq.visitor.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
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
    public Stream<ExtensionalDataNode> transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    /**
     * Blocks
     */
    @Override
    public Stream<ExtensionalDataNode> transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    /**
     * Only considers the left child
     */
    @Override
    public Stream<ExtensionalDataNode> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return leftChild.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }

    /**
     * TODO: try to extract some common data nodes
     */
    @Override
    public Stream<ExtensionalDataNode> transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return Stream.empty();
    }
}
