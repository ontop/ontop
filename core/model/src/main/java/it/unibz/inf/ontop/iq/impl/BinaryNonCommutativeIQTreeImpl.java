package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class BinaryNonCommutativeIQTreeImpl extends AbstractCompositeIQTree<BinaryNonCommutativeOperatorNode>
        implements BinaryNonCommutativeIQTree {

    private final IQTree leftIQTree;
    private final IQTree rightIQTree;
    private final boolean isLifted;

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild, @Assisted("right") IQTree rightChild,
                                           @Assisted boolean isLifted) {
        super(rootNode, ImmutableList.of(leftChild, rightChild));
        this.leftIQTree = leftChild;
        this.rightIQTree = rightChild;
        this.isLifted = isLifted;
    }

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild, @Assisted("right") IQTree rightChild) {
        this(rootNode, leftChild, rightChild, false);
    }

    @Override
    public IQTree getLeftChild() {
        return leftIQTree;
    }

    @Override
    public IQTree getRightChild() {
        return rightIQTree;
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        if (isLifted)
            return this;
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }
}
