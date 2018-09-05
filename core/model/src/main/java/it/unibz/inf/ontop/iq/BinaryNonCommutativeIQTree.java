package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;

public interface BinaryNonCommutativeIQTree extends CompositeIQTree<BinaryNonCommutativeOperatorNode> {

    IQTree getLeftChild();

    IQTree getRightChild();
}
