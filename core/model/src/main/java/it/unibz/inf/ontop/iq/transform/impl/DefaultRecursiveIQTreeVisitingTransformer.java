package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;

import java.util.function.Function;

/**
 *  * A superclass for {@code IQTreeVisitor}s that transform a given {@code IQTree} into an {@code IQTree}.
 *
 * Method {@code transformLeaf} returns the tree unchanged.
 * Method {@code transformUnaryNode}, {@code transformNaryCommutativeNode}
 * or {@code transformBinaryNonCommutativeNode} apply the transformer to
 * their children and, if the result is different, creates a new subtree;
 * otherwise, the input tree is reused (to reuse the {@code IQTreeCache} object).
 * <p>
 * To be extended by overloading the methods of interest.
 */

public abstract class DefaultRecursiveIQTreeVisitingTransformer extends DefaultIQTreeVisitingTransformer {

    protected final IntermediateQueryFactory iqFactory;

    protected DefaultRecursiveIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    protected final IQTree transformLeaf(LeafIQTree leaf){
        return leaf;
    }

    @Override
    protected final IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
        return transformUnaryNode(tree, node, child, this::transform);
    }

    protected final IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child,
                                              Function<IQTree, IQTree> childTransformation) {
        return withTransformedChild(tree, childTransformation.apply(child));
    }

    protected final IQTree withTransformedChild(UnaryIQTree tree, IQTree newChild) {
        return newChild.equals(tree.getChild())
                ? tree
                : iqFactory.createUnaryIQTree(tree.getRootNode(), newChild);
    }

    @Override
    protected final IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode node, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, node, children, this::transform);
    }

    protected final IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode node, ImmutableList<IQTree> children,
                                                        Function<IQTree, IQTree> childTransformation) {
        return withTransformedChildren(tree, NaryIQTreeTools.transformChildren(children, childTransformation));
    }

    protected final IQTree withTransformedChildren(NaryIQTree tree, ImmutableList<IQTree> newChildren) {
        return newChildren.equals(tree.getChildren())
                ? tree
                : iqFactory.createNaryIQTree(tree.getRootNode(), newChildren);
    }

    @Override
    protected final IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode node, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, node, leftChild, rightChild, this::transform);
    }

    protected final IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                             IQTree leftChild, IQTree rightChild,
                                                             Function<IQTree, IQTree> childTransformation) {
        return withTransformedChildren(tree, childTransformation.apply(leftChild), childTransformation.apply(rightChild));
    }

    protected final IQTree withTransformedChildren(BinaryNonCommutativeIQTree tree, IQTree newLeftChild, IQTree newRightChild) {
        return newLeftChild.equals(tree.getLeftChild()) && newRightChild.equals(tree.getRightChild())
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(tree.getRootNode(), newLeftChild, newRightChild);
    }
}
