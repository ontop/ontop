package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * To be extended by overloading the methods of interest.
 */
public abstract class DefaultRecursiveIQTreeVisitingTransformer implements IQTreeVisitingTransformer {

    protected final IntermediateQueryFactory iqFactory;

    protected DefaultRecursiveIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    protected DefaultRecursiveIQTreeVisitingTransformer(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
        return transformLeaf(dataNode);
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
        return transformLeaf(dataNode);
    }

    @Override
    public IQTree transformEmpty(EmptyNode node) {
        return transformLeaf(node);
    }

    @Override
    public IQTree transformTrue(TrueNode node) {
        return transformLeaf(node);
    }

    @Override
    public IQTree transformNonStandardLeafNode(LeafIQTree leafNode) {
        return transformLeaf(leafNode);
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(rootNode, child);
    }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode rootNode, IQTree child) {
        return transformUnaryNode(rootNode, child);
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        return transformUnaryNode(rootNode, child);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return transformUnaryNode(rootNode, child);
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        return transformUnaryNode(sliceNode, child);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return transformUnaryNode(rootNode, child);
    }

    @Override
    public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return transformUnaryNode(rootNode, child);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(rootNode, leftChild, rightChild);
    }

    @Override
    public IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(rootNode, leftChild, rightChild);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(rootNode, children);
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(rootNode, children);
    }

    @Override
    public IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(rootNode, children);
    }

    protected IQTree transformLeaf(LeafIQTree leaf){
        return leaf;
    }

    protected IQTree transformUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
        return iqFactory.createUnaryIQTree(rootNode, child.acceptTransformer(this));
    }

    protected IQTree transformNaryCommutativeNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(
                rootNode,
                children.stream()
                        .map(t -> t.acceptTransformer(this))
                        .collect(ImmutableCollectors.toList())
        );
    }

    protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
        return iqFactory.createBinaryNonCommutativeIQTree(
                rootNode,
                leftChild.acceptTransformer(this),
                rightChild.acceptTransformer(this)
        );
    }
}
