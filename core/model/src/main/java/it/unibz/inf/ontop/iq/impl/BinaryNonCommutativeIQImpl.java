package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQ;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class BinaryNonCommutativeIQImpl extends AbstractCompositeIQ<BinaryNonCommutativeOperatorNode>
        implements BinaryNonCommutativeIQ {

    private final IQ leftIQ;
    private final IQ rightIQ;
    private final boolean isLifted;

    @AssistedInject
    private BinaryNonCommutativeIQImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                       @Assisted("left") IQ leftChild, @Assisted("right") IQ rightChild,
                                       @Assisted boolean isLifted) {
        super(rootNode, ImmutableList.of(leftChild, rightChild));
        this.leftIQ = leftChild;
        this.rightIQ = rightChild;
        this.isLifted = isLifted;
    }

    @AssistedInject
    private BinaryNonCommutativeIQImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                       @Assisted("left") IQ leftChild, @Assisted("right") IQ rightChild) {
        this(rootNode, leftChild, rightChild, false);
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
    public IQ liftBinding(VariableGenerator variableGenerator) {
        if (isLifted)
            return this;
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public IQ applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                          Optional<ImmutableExpression> constraint) {
        throw new RuntimeException("TODO: implement it");
    }
}
