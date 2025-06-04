package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;

/**
 * Methods transformLeaf, transformUnaryNode, transformNaryCommutativeNode
 * and transformBinaryNonCommutativeNode simply return the tree unchanged.
 *
 * To be extended by overriding the methods of interest.
 */
public abstract class DefaultNonRecursiveIQTreeTransformer extends DefaultIQTreeVisitingTransformer {

    @Override
    protected IQTree transformLeaf(LeafIQTree leaf) {
        return leaf;
    }

    @Override
    protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode node, IQTree child) {
        return tree;
    }

    @Override
    protected IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode node, ImmutableList<IQTree> children) {
        return tree;
    }

    @Override
    protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode node, IQTree leftChild, IQTree rightChild) {
        return tree;
    }
}
