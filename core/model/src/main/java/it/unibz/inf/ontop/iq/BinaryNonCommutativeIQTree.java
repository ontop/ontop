package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface BinaryNonCommutativeIQTree extends CompositeIQTree<BinaryNonCommutativeOperatorNode> {

    IQTree getLeftChild();

    IQTree getRightChild();
}
