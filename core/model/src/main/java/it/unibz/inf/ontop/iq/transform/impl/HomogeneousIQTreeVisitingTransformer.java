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
    public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
        return dataNode.acceptNodeTransformer(nodeTransformer);
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
        return dataNode.acceptNodeTransformer(nodeTransformer);
    }

    @Override
    public IQTree transformEmpty(EmptyNode node) {
        return node.acceptNodeTransformer(nodeTransformer);
    }

    @Override
    public IQTree transformTrue(TrueNode node) {
        return node.acceptNodeTransformer(nodeTransformer);
    }

    @Override
    public IQTree transformValues(ValuesNode node) {
        return node.acceptNodeTransformer(nodeTransformer);
    }

    @Override
    public IQTree transformNonStandardLeafNode(LeafIQTree leafNode) {
        return leafNode.acceptNodeTransformer(nodeTransformer);
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), child);
    }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), child);
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), child);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), child);
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        return transformUnaryNode(tree, sliceNode.acceptNodeTransformer(nodeTransformer), child);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), child);
    }

    @Override
    public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), child);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), leftChild, rightChild);
    }

    @Override
    public IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                               IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), leftChild, rightChild);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), children);
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), children);
    }

    @Override
    public IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode.acceptNodeTransformer(nodeTransformer), children);
    }
}
