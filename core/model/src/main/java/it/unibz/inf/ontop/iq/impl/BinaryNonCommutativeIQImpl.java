package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQ;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;


public class BinaryNonCommutativeIQImpl extends AbstractCompositeIQ implements BinaryNonCommutativeIQ {

    private final IQ leftIQ;
    private final IQ rightIQ;

    protected BinaryNonCommutativeIQImpl(BinaryNonCommutativeOperatorNode rootNode, IQ leftIQ, IQ rightIQ) {
        super(rootNode, ImmutableList.of(leftIQ, rightIQ));
        this.leftIQ = leftIQ;
        this.rightIQ = rightIQ;
    }

    @Override
    public IQ getLeftChild() {
        return leftIQ;
    }

    @Override
    public IQ getLeftRight() {
        return rightIQ;
    }
}
