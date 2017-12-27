package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.Optional;


public class BinaryNonCommutativeIQTreeImpl extends AbstractCompositeIQTree<BinaryNonCommutativeOperatorNode>
        implements BinaryNonCommutativeIQTree {

    private final IQTree leftChild;
    private final IQTree rightChild;
    // LAZY
    @Nullable
    private ImmutableSet<Variable> nullableVariables;

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild, @Assisted("right") IQTree rightChild,
                                           @Assisted IQProperties iqProperties) {
        super(rootNode, ImmutableList.of(leftChild, rightChild), iqProperties);
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild,
                                           @Assisted("right") IQTree rightChild) {
        this(rootNode, leftChild, rightChild, new IQPropertiesImpl());
    }

    @Override
    public IQTree getLeftChild() {
        return leftChild;
    }

    @Override
    public IQTree getRightChild() {
        return rightChild;
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        IQProperties properties = getProperties();
        if (properties.isLifted())
            return this;
        return getRootNode().liftBinding(leftChild, rightChild, variableGenerator, properties);
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, leftChild, rightChild);
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable)
                && getRootNode().isConstructed(variable, leftChild, rightChild);
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables() {
        if (nullableVariables == null)
            nullableVariables = getRootNode().getNullableVariables(leftChild, rightChild);
        return nullableVariables;
    }
}
