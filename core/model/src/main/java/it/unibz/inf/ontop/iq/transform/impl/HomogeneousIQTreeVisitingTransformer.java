package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * Uses a HomogeneousQueryNodeTransformer
 */
public class HomogeneousIQTreeVisitingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    private final HomogeneousQueryNodeTransformer nodeTransformer;

    public HomogeneousIQTreeVisitingTransformer(HomogeneousQueryNodeTransformer nodeTransformer,
                                                IntermediateQueryFactory iqFactory) {
        super(iqFactory);
        this.nodeTransformer = nodeTransformer;
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode node) {
        return nodeTransformer.transform(node);
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode node) {
        return nodeTransformer.transform(node);
    }

    @Override
    public IQTree transformEmpty(EmptyNode node) {
        return nodeTransformer.transform(node);
    }

    @Override
    public IQTree transformTrue(TrueNode node) {
        return nodeTransformer.transform(node);
    }

    @Override
    public IQTree transformValues(ValuesNode node) {
        return nodeTransformer.transform(node);
    }

    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(sliceNode), child);
    }

    @Override
    public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, nodeTransformer.transform(rootNode), leftChild, rightChild);
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, nodeTransformer.transform(rootNode), children);
    }

    @Override
    public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, nodeTransformer.transform(rootNode), children);
    }
}
