package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
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
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(sliceNode), child);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformFlatten(IQTree tree, FlattenNode rootNode, IQTree child) {
        return transformUnaryNode(tree, nodeTransformer.transform(rootNode), child);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, nodeTransformer.transform(rootNode), leftChild, rightChild);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, nodeTransformer.transform(rootNode), children);
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, nodeTransformer.transform(rootNode), children);
    }


    @Override
    public IQTree transformNonStandardLeafNode(LeafIQTree leafNode) {
        throw new RuntimeException("A non-standard leaf node was encountered: " + leafNode);
    }

    @Override
    public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        throw new RuntimeException("A non-standard unary node was encountered: " + rootNode);
    }

    @Override
    public IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                               IQTree leftChild, IQTree rightChild) {
        throw new RuntimeException("A non-standard binary non-commutative node was encountered: " + rootNode);
    }

    @Override
    public IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        throw new RuntimeException("A non-standard n-ary node was encountered: " + rootNode);
    }
}
