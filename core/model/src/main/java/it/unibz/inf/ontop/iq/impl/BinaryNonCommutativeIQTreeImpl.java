package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.BinaryNonCommutativeOperatorNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class BinaryNonCommutativeIQTreeImpl extends AbstractCompositeIQTree<BinaryNonCommutativeOperatorNode>
        implements BinaryNonCommutativeIQTree {

    /**
     * See {@link IntermediateQueryFactory#createBinaryNonCommutativeIQTree(
     *      BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild, IQTreeCache treeCache)}
     */
    @SuppressWarnings("unused")
    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild, @Assisted("right") IQTree rightChild,
                                           @Assisted IQTreeCache treeCache, IQTreeTools iqTreeTools,
                                           IntermediateQueryFactory iqFactory, TermFactory termFactory,
                                           OntopModelSettings settings, SubstitutionFactory substitutionFactory) {
        super(rootNode, ImmutableList.of(leftChild, rightChild), treeCache, iqTreeTools, iqFactory, termFactory, substitutionFactory);

        if (settings.isTestModeEnabled())
            validate();
    }

    /**
     * See {@link IntermediateQueryFactory#createBinaryNonCommutativeIQTree(
     *      BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild)}
     */
    @SuppressWarnings("unused")
    @AssistedInject
    private BinaryNonCommutativeIQTreeImpl(@Assisted BinaryNonCommutativeOperatorNode rootNode,
                                           @Assisted("left") IQTree leftChild,
                                           @Assisted("right") IQTree rightChild,
                                           IQTreeTools iqTreeTools,
                                           IntermediateQueryFactory iqFactory,
                                           TermFactory termFactory,
                                           OntopModelSettings settings, SubstitutionFactory substitutionFactory,
                                           IQTreeCache freshTreeCash) {
        this(rootNode, leftChild, rightChild, freshTreeCash, iqTreeTools, iqFactory, termFactory, settings, substitutionFactory);
    }

    @Override
    public IQTree getLeftChild() {
        return getChildren().get(0);
    }

    @Override
    public IQTree getRightChild() {
        return getChildren().get(1);
    }

    @Override
    protected IQTree doApplyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            VariableGenerator variableGenerator) {
        return getRootNode().applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, getLeftChild(), getRightChild(), variableGenerator);
    }


    @Override
    protected IQTree doNormalizeForOptimization(VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return getRootNode().normalizeForOptimization(getLeftChild(), getRightChild(), variableGenerator, treeCache);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return getRootNode().liftIncompatibleDefinitions(variable, getLeftChild(), getRightChild(), variableGenerator);
    }

    @Override
    protected IQTree applyNonEmptyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution) {
        return iqFactory.createBinaryNonCommutativeIQTree(
                getRootNode().applyFreshRenaming(renamingSubstitution),
                getLeftChild().applyFreshRenaming(renamingSubstitution),
                getRightChild().applyFreshRenaming(renamingSubstitution),
                getTreeCache().applyFreshRenaming(renamingSubstitution));
    }

    @Override
    protected IQTree applyRegularDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, getLeftChild(), getRightChild(), variableGenerator);
    }


    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable) && getRootNode().isConstructed(variable, getLeftChild(), getRightChild());
    }

    @Override
    protected boolean computeIsDistinct() {
        return getRootNode().isDistinct(this, getLeftChild(), getRightChild());
    }

    @Override
    protected VariableNullability computeVariableNullability() {
        return getRootNode().getVariableNullability(getLeftChild(), getRightChild());
    }

    @Override
    protected IQTree doPropagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        return getRootNode().propagateDownConstraint(constraint, getLeftChild(), getRightChild(), variableGenerator);
    }

    @Override
    protected IQTree doRemoveDistincts(IQTreeCache treeCache) {
        return getRootNode().removeDistincts(getLeftChild(), getRightChild(), treeCache);
    }

    @Override
    protected IQTree createIQTree(ImmutableList<IQTree> newChildren) {
        return iqFactory.createBinaryNonCommutativeIQTree(getRootNode(),
                newChildren.get(0), newChildren.get(1));
    }

    @Override
    protected ImmutableSet<Substitution<NonVariableTerm>> computePossibleVariableDefinitions() {
        return getRootNode().getPossibleVariableDefinitions(getLeftChild(), getRightChild());
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(getLeftChild(), getRightChild());
    }

    @Override
    protected ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        return getRootNode().inferUniqueConstraints(getLeftChild(), getRightChild());
    }

    @Override
    protected ImmutableSet<Variable> computeStrictDependents() {
        return getRootNode().inferStrictDependents(this, getLeftChild(), getRightChild());
    }

    @Override
    protected VariableNonRequirement computeVariableNonRequirement() {
        return getRootNode().computeVariableNonRequirement(getLeftChild(), getRightChild());
    }

    @Override
    protected FunctionalDependencies computeFunctionalDependencies() {
        return getRootNode().inferFunctionalDependencies(getLeftChild(), getRightChild(), inferUniqueConstraints(), getVariables());
    }
}
