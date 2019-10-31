package it.unibz.inf.ontop.iq.visitor.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visitor.RequiredDataAtomExtractor;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;


@Singleton
public class RequiredDataAtomExtractorImpl implements RequiredDataAtomExtractor {

    @Inject
    protected RequiredDataAtomExtractorImpl() {
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitIntensionalData(IntensionalDataNode dataNode) {
        return Stream.empty();
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.of(dataNode.getProjectionAtom());
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitEmpty(EmptyNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitTrue(TrueNode node) {
        return Stream.empty();
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitNative(NativeNode nativeNode) {
        return Stream.empty();
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitNonStandardLeafNode(LeafIQTree leafNode) {
        return Stream.empty();
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitConstruction(ConstructionNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    /**
     * Blocks
     */
    @Override
    public Stream<DataAtom<RelationPredicate>> visitAggregation(AggregationNode aggregationNode, IQTree child) {
        return Stream.empty();
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitFilter(FilterNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitDistinct(DistinctNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitSlice(SliceNode sliceNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitOrderBy(OrderByNode rootNode, IQTree child) {
        return child.acceptVisitor(this);
    }

    /**
     * Blocks by default
     */
    @Override
    public Stream<DataAtom<RelationPredicate>> visitNonStandardUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
        return Stream.empty();
    }

    /**
     * Only considers the left child
     */
    @Override
    public Stream<DataAtom<RelationPredicate>> visitLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return leftChild.acceptVisitor(this);
    }

    /**
     * Blocks by default
     */
    @Override
    public Stream<DataAtom<RelationPredicate>> visitNonStandardBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        return Stream.empty();
    }

    @Override
    public Stream<DataAtom<RelationPredicate>> visitInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.acceptVisitor(this));
    }

    /**
     * TODO: try to extract some common data nodes
     */
    @Override
    public Stream<DataAtom<RelationPredicate>> visitUnion(UnionNode rootNode, ImmutableList<IQTree> children) {
        return Stream.empty();
    }

    /**
     * Blocks by default
     */
    @Override
    public Stream<DataAtom<RelationPredicate>> visitNonStandardNaryNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return Stream.empty();
    }
}
