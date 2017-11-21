package it.unibz.inf.ontop.iq;

import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;

public interface BinaryNonCommutativeIQ extends CompositeIQ<BinaryNonCommutativeOperatorNode> {

    IQ getLeftChild();

    IQ getLeftRight();
}
