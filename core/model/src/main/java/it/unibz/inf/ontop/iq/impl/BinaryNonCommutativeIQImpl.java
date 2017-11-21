package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQ;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;


public class BinaryNonCommutativeIQImpl extends AbstractCompositeIQ implements BinaryNonCommutativeIQ {

    private final IQ leftIQ;
    private final IQ rightIQ;

    @AssistedInject
    private BinaryNonCommutativeIQImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                       @Assisted("left") IQ leftChild, @Assisted("right") IQ rightChild) {
        super(rootNode, ImmutableList.of(leftChild, rightChild));
        this.leftIQ = leftChild;
        this.rightIQ = rightChild;
    }

    @Override
    public IQ getLeftChild() {
        return leftIQ;
    }

    @Override
    public IQ getLeftRight() {
        return rightIQ;
    }

    @Override
    public IQ liftBinding() {
        throw new RuntimeException("TODO: implement it");
    }
}
