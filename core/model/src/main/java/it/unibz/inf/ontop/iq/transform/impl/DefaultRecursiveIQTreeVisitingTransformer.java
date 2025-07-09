package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;

/**
 * Method transformLeaf simply returns the tree unchanged.
 * Method transformUnaryNode, transformNaryCommutativeNode
 * or transformBinaryNonCommutativeNode apply the transformer to
 * their children and if the result is different, creates a new subtree;
 * otherwise, the input tree is reused.
 *
 * To be extended by overloading the methods of interest.
 */
public abstract class DefaultRecursiveIQTreeVisitingTransformer extends DefaultIQTreeVisitingTransformer {

    protected final IntermediateQueryFactory iqFactory;

    protected DefaultRecursiveIQTreeVisitingTransformer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    protected boolean treesEqual(IQTree tree1, IQTree tree2) {
        return tree1.equals(tree2);
    }

    protected boolean treesEqual(ImmutableList<IQTree> trees1, ImmutableList<IQTree> trees2) {
        return trees1.equals(trees2);
    }

    @Override
    protected final IQTree transformLeaf(LeafIQTree leaf){
        return leaf;
    }

    @Override
    protected final IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
        return withTransformedChild(tree, transformChild(child));
    }

    protected final UnaryIQTree withTransformedChild(UnaryIQTree tree, IQTree newChild) {
        return treesEqual(newChild, tree.getChild())
                ? tree
                : iqFactory.createUnaryIQTree(tree.getRootNode(), newChild);
    }

    @Override
    protected final IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode node, ImmutableList<IQTree> children) {
        return withTransformedChildren(tree, NaryIQTreeTools.transformChildren(children, this::transformChild));
    }

    protected final NaryIQTree withTransformedChildren(NaryIQTree tree, ImmutableList<IQTree> newChildren) {
        return treesEqual(newChildren, tree.getChildren())
                ? tree
                : iqFactory.createNaryIQTree(tree.getRootNode(), newChildren);
    }

    @Override
    protected final IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode node, IQTree leftChild, IQTree rightChild) {
        return withTransformedChildren(tree, transformChild(leftChild), transformChild(rightChild));
    }

    protected final BinaryNonCommutativeIQTree withTransformedChildren(BinaryNonCommutativeIQTree tree, IQTree newLeftChild, IQTree newRightChild) {
        return treesEqual(newLeftChild, tree.getLeftChild()) && treesEqual(newRightChild, tree.getRightChild())
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(tree.getRootNode(), newLeftChild, newRightChild);
    }
}
