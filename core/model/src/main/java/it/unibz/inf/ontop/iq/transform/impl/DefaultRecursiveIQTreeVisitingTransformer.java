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

    protected boolean nodesEqual(QueryNode node1, QueryNode node2) {
        return node1.equals(node2);
    }

    @Override
    protected IQTree transformLeaf(LeafIQTree leaf){
        return leaf;
    }

    @Override
    protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
        IQTree newChild = transformChild(child);
        return treesEqual(newChild, child) && nodesEqual(node, tree.getRootNode())
                ? tree
                : iqFactory.createUnaryIQTree(node, newChild);
    }

    @Override
    protected final IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode node, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> newChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
        return treesEqual(newChildren, children) && nodesEqual(node, tree.getRootNode())
                ? tree
                : iqFactory.createNaryIQTree(node, newChildren);
    }

    @Override
    protected final IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode node, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = transformChild(leftChild);
        IQTree newRightChild = transformChild(rightChild);
        return treesEqual(newLeftChild, leftChild) && treesEqual(newRightChild, rightChild) && nodesEqual(node, tree.getRootNode())
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(node, newLeftChild, newRightChild);
    }
}
