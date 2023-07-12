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
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class BinaryNonCommutativeIQTreeImpl extends AbstractCompositeIQTree<BinaryNonCommutativeOperatorNode>
        implements BinaryNonCommutativeIQTree {

    private final IQTree leftChild;
    private final IQTree rightChild;


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
        this.leftChild = leftChild;
        this.rightChild = rightChild;

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
    public <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return getRootNode().acceptTransformer(this, transformer, leftChild, rightChild, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(visitor, leftChild, rightChild);
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        IQTreeCache treeCache = getTreeCache();
        if (treeCache.isNormalizedForOptimization())
            return this;
        return getRootNode().normalizeForOptimization(leftChild, rightChild, variableGenerator, treeCache);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return getRootNode().liftIncompatibleDefinitions(variable, leftChild, rightChild, variableGenerator);
    }

    @Override
    protected IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, boolean alreadyNormalized) {
        InjectiveSubstitution<Variable> selectedSubstitution = alreadyNormalized
                ? renamingSubstitution
                : renamingSubstitution.restrictDomainTo(getVariables());

        return selectedSubstitution.isEmpty()
                ? this
                : getRootNode().applyFreshRenaming(renamingSubstitution, leftChild, rightChild, getTreeCache());
    }

    @Override
    protected IQTree applyRegularDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, leftChild, rightChild, variableGenerator);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            VariableGenerator variableGenerator) {
        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitutionWithoutOptimizing(s, leftChild, rightChild, variableGenerator))
                    .orElse(this);
        }
        catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable)
                && getRootNode().isConstructed(variable, leftChild, rightChild);
    }

    @Override
    protected boolean computeIsDistinct() {
        return getRootNode().isDistinct(this, leftChild, rightChild);
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    protected VariableNullability computeVariableNullability() {
        return getRootNode().getVariableNullability(leftChild, rightChild);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        IQTree newTree = getRootNode().propagateDownConstraint(constraint, leftChild, rightChild, variableGenerator);
        return newTree.equals(this) ? this : newTree;
    }

    @Override
    public IQTree removeDistincts() {
        IQTreeCache properties = getTreeCache();
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
    protected ImmutableSet<Substitution<NonVariableTerm>> computePossibleVariableDefinitions() {
        return getRootNode().getPossibleVariableDefinitions(leftChild, rightChild);
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(leftChild, rightChild);
    }

    @Override
    protected ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        return getRootNode().inferUniqueConstraints(leftChild, rightChild);
    }

    @Override
    protected VariableNonRequirement computeVariableNonRequirement() {
        return getRootNode().computeNotInternallyRequiredVariables(leftChild, rightChild);
    }

    @Override
    protected FunctionalDependencies computeFunctionalDependencies() {
        return getRootNode().inferFunctionalDependencies(leftChild, rightChild, inferUniqueConstraints(), getVariables());
    }
}
