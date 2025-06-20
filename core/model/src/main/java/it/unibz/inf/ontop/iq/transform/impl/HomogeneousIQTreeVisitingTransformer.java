package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

import java.util.stream.IntStream;

/**
 * Uses a HomogeneousQueryNodeTransformer
 */
public final class HomogeneousIQTreeVisitingTransformer extends DefaultIQTreeVisitingTransformer {

    private final HomogeneousQueryNodeTransformer nodeTransformer;
    private final IntermediateQueryFactory iqFactory;

    public HomogeneousIQTreeVisitingTransformer(HomogeneousQueryNodeTransformer nodeTransformer,
                                                IntermediateQueryFactory iqFactory) {
        this.nodeTransformer = nodeTransformer;
        this.iqFactory = iqFactory;
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

    @Override
    protected final IQTree transformLeaf(LeafIQTree leaf) {
        throw new MinorOntopInternalBugException("should never happen");
    }

    @Override
    protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode newNode, IQTree child) {
        IQTree newChild = transformChild(child);
        return (newChild == child && newNode.equals(tree.getRootNode()))
                ? tree
                : iqFactory.createUnaryIQTree(newNode,  newChild);
    }

    @Override
    protected IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode newNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
        return IntStream.range(0, children.size())
                .allMatch(i -> newChildren.get(i) == children.get(i)
                        && newNode.equals(tree.getRootNode()))
                ? tree
                : iqFactory.createNaryIQTree(newNode,  newChildren);
    }

    @Override
    protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode newNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = transformChild(leftChild);
        IQTree newRightChild = transformChild(rightChild);
        return (newLeftChild == leftChild && newRightChild == rightChild && newNode.equals(tree.getRootNode()))
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(newNode,  newLeftChild, newRightChild);
    }
}
