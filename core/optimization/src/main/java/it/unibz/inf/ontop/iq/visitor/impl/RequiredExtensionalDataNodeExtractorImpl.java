package it.unibz.inf.ontop.iq.visitor.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.impl.AbstractIQTreeToStreamVisitingTransformer;
import it.unibz.inf.ontop.iq.visitor.RequiredExtensionalDataNodeExtractor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;


/**
 * Looks for extensional data nodes that are required to provide tuples.
 *
 * For instance, excludes data atoms only appearing on the right of a LJ.
 *
 * MAY BE INCOMPLETE
 *
 */


@Singleton
public class RequiredExtensionalDataNodeExtractorImpl extends AbstractIQTreeToStreamVisitingTransformer<ExtensionalDataNode>
        implements RequiredExtensionalDataNodeExtractor {

    @Inject
    protected RequiredExtensionalDataNodeExtractorImpl() {
    }

    @Override
    public Stream<ExtensionalDataNode> transform(IQTree tree) {
        return tree.acceptVisitor(this);
    }

    @Override
    public Stream<ExtensionalDataNode> transformExtensionalData(ExtensionalDataNode dataNode) {
        return Stream.of(dataNode);
    }

    @Override
    public Stream<ExtensionalDataNode> transformAggregation(UnaryIQTree tree, AggregationNode aggregationNode, IQTree child) {
        // blocks
        return Stream.empty();
    }

    @Override
    public Stream<ExtensionalDataNode> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        // Only considers the left child
        return transformChild(leftChild);
    }

    /**
     * TODO: try to extract some common data nodes
     */
    @Override
    public Stream<ExtensionalDataNode> transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return Stream.empty();
    }
}
