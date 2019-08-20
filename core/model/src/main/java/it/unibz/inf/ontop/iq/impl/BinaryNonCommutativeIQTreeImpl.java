package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
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
    private VariableNullability variableNullability;
    @Nullable
    private ImmutableSet<ImmutableSet<Variable>> uniqueConstraints;
    @Nullable
    private Boolean isDistinct;
    @Nullable
    private ImmutableSet<ImmutableSubstitution<NonVariableTerm>> possibleVariableDefinitions;

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild, @Assisted("right") IQTree rightChild,
                                           @Assisted IQProperties iqProperties, IQTreeTools iqTreeTools,
                                           IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        super(rootNode, ImmutableList.of(leftChild, rightChild), iqProperties, iqTreeTools, iqFactory);
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.variableNullability = null;
        this.possibleVariableDefinitions = null;
        this.isDistinct = null;

        if (settings.isTestModeEnabled())
            validate();
    }

    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild,
                                           @Assisted("right") IQTree rightChild,
                                           IQTreeTools iqTreeTools,
                                           IntermediateQueryFactory iqFactory,
                                           OntopModelSettings settings) {
        this(rootNode, leftChild, rightChild, iqFactory.createIQProperties(), iqTreeTools, iqFactory, settings);
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
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return getRootNode().acceptTransformer(this, transformer, leftChild, rightChild);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(visitor, leftChild, rightChild);
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        IQProperties properties = getProperties();
        if (properties.isNormalizedForOptimization())
            return this;
        return getRootNode().normalizeForOptimization(leftChild, rightChild, variableGenerator, properties);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable) {
        return getRootNode().liftIncompatibleDefinitions(variable, leftChild, rightChild);
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint) {

        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitution(s, constraint, leftChild, rightChild))
                    .orElseGet(() -> constraint
                            .map(this::propagateDownConstraint)
                            .orElse(this));

        } catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitutionWithoutOptimizing(s, leftChild, rightChild))
                    .orElse(this);
        } catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable)
                && getRootNode().isConstructed(variable, leftChild, rightChild);
    }

    @Override
    public boolean isDistinct() {
        if (isDistinct == null)
            isDistinct = getRootNode().isDistinct(leftChild, rightChild);
        return isDistinct;
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public VariableNullability getVariableNullability() {
        if (variableNullability == null)
            variableNullability = getRootNode().getVariableNullability(leftChild, rightChild);
        return variableNullability;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        return getRootNode().propagateDownConstraint(constraint, leftChild, rightChild);
    }

    @Override
    public IQTree removeDistincts() {
        IQProperties properties = getProperties();
        return properties.areDistinctAlreadyRemoved()
                ? this
                : getRootNode().removeDistincts(leftChild, rightChild, properties);
    }

    @Override
    public IQTree replaceSubTree(IQTree subTreeToReplace, IQTree newSubTree) {
        if (equals(subTreeToReplace))
            return newSubTree;

        return iqFactory.createBinaryNonCommutativeIQTree(getRootNode(),
                leftChild.replaceSubTree(subTreeToReplace, newSubTree),
                rightChild.replaceSubTree(subTreeToReplace, newSubTree));
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        if (possibleVariableDefinitions == null)
            possibleVariableDefinitions = getRootNode().getPossibleVariableDefinitions(leftChild, rightChild);
        return possibleVariableDefinitions;
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(leftChild, rightChild);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints() {
        if (uniqueConstraints == null) {
            uniqueConstraints = getRootNode().inferUniqueConstraints(leftChild, rightChild);
        }
        return uniqueConstraints;
    }
}
