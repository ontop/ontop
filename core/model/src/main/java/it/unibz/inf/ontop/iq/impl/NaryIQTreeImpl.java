package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
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
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class NaryIQTreeImpl extends AbstractCompositeIQTree<NaryOperatorNode> implements NaryIQTree {

    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           @Assisted IQTreeCache treeCache,
                           IQTreeTools iqTreeTools,
                           IntermediateQueryFactory iqFactory, TermFactory termFactory, OntopModelSettings settings, SubstitutionFactory substitutionFactory) {
        super(rootNode, children, treeCache, iqTreeTools, iqFactory, termFactory, substitutionFactory);
        if (children.size() < 2)
            throw new IllegalArgumentException("At least two children are required for a n-ary node");

        if (settings.isTestModeEnabled())
            validate();
    }


    @AssistedInject
    private NaryIQTreeImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQTree> children,
                           IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, TermFactory termFactory,
                           OntopModelSettings settings, SubstitutionFactory substitutionFactory,
                           IQTreeCache freshTreeCache) {
        this(rootNode, children, freshTreeCache, iqTreeTools, iqFactory, termFactory, settings, substitutionFactory);
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(getChildren());
    }

    @Override
    protected VariableNullability computeVariableNullability() {
        return getRootNode().getVariableNullability(getChildren());
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return getRootNode().acceptTransformer(this, transformer, getChildren());
    }

    @Override
    public <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return getRootNode().acceptTransformer(this, transformer, getChildren(), context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(visitor, getChildren());
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        return getTreeCache().isNormalizedForOptimization()
                ? this
                : getRootNode().normalizeForOptimization(getChildren(), variableGenerator, getTreeCache());
    }

    /**
     * TODO: use the properties for optimization purposes?
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return getRootNode().liftIncompatibleDefinitions(variable, getChildren(), variableGenerator);
    }

    @Override
    protected IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, boolean alreadyNormalized) {
        InjectiveSubstitution<Variable> selectedSubstitution = alreadyNormalized
                ? renamingSubstitution
                : renamingSubstitution.restrictDomainTo(getVariables());

        return selectedSubstitution.isEmpty()
                ? this
                : getRootNode().applyFreshRenaming(renamingSubstitution, getChildren(), getTreeCache());
    }

    @Override
    protected IQTree applyRegularDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, getChildren(), variableGenerator);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            VariableGenerator variableGenerator) {
        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitutionWithoutOptimizing(s, getChildren(), variableGenerator))
                    .orElse(this);
        }
        catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    /**
     * TODO: should we cache the boolean?
     */
    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable) && getRootNode().isConstructed(variable, getChildren());
    }

    @Override
    protected boolean computeIsDistinct() {
        return getRootNode().isDistinct(this, getChildren());
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        IQTree newTree = getRootNode().propagateDownConstraint(constraint, getChildren(), variableGenerator);
        return newTree.equals(this) ? this : newTree;
    }

    @Override
    public IQTree replaceSubTree(IQTree subTreeToReplace, IQTree newSubTree) {
        if (equals(subTreeToReplace))
            return newSubTree;

        ImmutableList<IQTree> newChildren = getChildren().stream()
                .map(c -> c.replaceSubTree(subTreeToReplace, newSubTree))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createNaryIQTree(getRootNode(), newChildren);
    }

    @Override
    protected ImmutableSet<Substitution<NonVariableTerm>> computePossibleVariableDefinitions() {
        return getRootNode().getPossibleVariableDefinitions(getChildren());
    }

    @Override
    public IQTree removeDistincts() {
        IQTreeCache treeCache = getTreeCache();
        return treeCache.areDistinctAlreadyRemoved()
                ? this
                : getRootNode().removeDistincts(getChildren(), treeCache);
    }

    @Override
    protected ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        return getRootNode().inferUniqueConstraints(getChildren());
    }

    @Override
    protected VariableNonRequirement computeVariableNonRequirement() {
        return getRootNode().computeVariableNonRequirement(getChildren());
    }

    @Override
    protected FunctionalDependencies computeFunctionalDependencies() {
        return getRootNode().inferFunctionalDependencies(getChildren(), inferUniqueConstraints(), getVariables());
    }
}
