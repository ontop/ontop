package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

/**
 * Applies the transformer to the children
 */
public class ChildTransformer implements IQTreeVisitingTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTransformer transformer;

    public ChildTransformer(IntermediateQueryFactory iqFactory, IQTreeTransformer transformer) {
        this.iqFactory = iqFactory;
        this.transformer = transformer;
    }

    protected final IQTree transformUnaryNode(IQTree initialTree, UnaryOperatorNode rootNode, IQTree child) {
        IQTree newChild = transformer.transform(child);
        return child.equals(newChild)
                ? initialTree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    protected final IQTree transformNaryCommutativeNode(IQTree initialTree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(transformer::transform)
                .collect(ImmutableCollectors.toList());

        return children.equals(newChildren)
                ? initialTree
                : iqFactory.createNaryIQTree(rootNode, newChildren);
    }

    protected final IQTree transformBinaryNonCommutativeNode(IQTree initialTree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild,
                                                       IQTree rightChild) {
        IQTree newLeftChild = transformer.transform(leftChild);
        IQTree newRightChild = transformer.transform(rightChild);

        return leftChild.equals(newLeftChild) && rightChild.equals(newRightChild)
                ? initialTree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
    }

    @Override
    public final IQTree transformIntensionalData(IntensionalDataNode rootNode) {
        return rootNode;
    }

    @Override
    public final IQTree transformExtensionalData(ExtensionalDataNode rootNode) {
        return rootNode;
    }

    @Override
    public final IQTree transformEmpty(EmptyNode rootNode) {
        return rootNode;
    }

    @Override
    public final IQTree transformTrue(TrueNode rootNode) {
        return rootNode;
    }

    @Override
    public final IQTree transformNonStandardLeafNode(LeafIQTree rootNode) {
        return rootNode;
    }

    @Override
    public final IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public final IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public final IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public final IQTree transformSlice(IQTree tree, SliceNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public final IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child);
    }

    @Override
    public final IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild);
    }

    @Override
    public final IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                                     IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild);
    }

    @Override
    public final IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children);
    }

    @Override
    public final IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children);
    }

    @Override
    public final IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children);
    }
}
