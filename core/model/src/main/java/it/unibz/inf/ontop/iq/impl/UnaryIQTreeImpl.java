package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
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

public class UnaryIQTreeImpl extends AbstractCompositeIQTree<UnaryOperatorNode> implements UnaryIQTree {

    @AssistedInject
    private UnaryIQTreeImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQTree child,
                            @Assisted IQTreeCache treeCache, IQTreeTools iqTreeTools,
                            IntermediateQueryFactory iqFactory, TermFactory termFactory, OntopModelSettings settings, SubstitutionFactory substitutionFactory) {
        super(rootNode, ImmutableList.of(child), treeCache, iqTreeTools, iqFactory, termFactory, substitutionFactory);

        if (settings.isTestModeEnabled())
            validate();
    }


    @AssistedInject
    private UnaryIQTreeImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQTree child, IQTreeTools iqTreeTools,
                            IntermediateQueryFactory iqFactory, TermFactory termFactory, OntopModelSettings settings, SubstitutionFactory substitutionFactory,
                            IQTreeCache freshTreeCache) {
        this(rootNode, child, freshTreeCache, iqTreeTools, iqFactory, termFactory, settings, substitutionFactory);
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        if (getTreeCache().isNormalizedForOptimization())
            return this;
        else
            return getRootNode().normalizeForOptimization(getChild(), variableGenerator, getTreeCache());
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return getRootNode().liftIncompatibleDefinitions(variable, getChild(), variableGenerator);
    }

    @Override
    protected IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, boolean alreadyNormalized) {
        InjectiveSubstitution<Variable> selectedSubstitution = alreadyNormalized
                ? renamingSubstitution
                : renamingSubstitution.restrictDomainTo(getVariables());

        return selectedSubstitution.isEmpty()
                ? this
                : getRootNode().applyFreshRenaming(renamingSubstitution, getChild(), getTreeCache());
    }

    @Override
    protected IQTree applyRegularDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                        Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, getChild(), variableGenerator);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator) {
        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitutionWithoutOptimizing(s, getChild(), variableGenerator))
                    .orElse(this);

        } catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable)
                && getRootNode().isConstructed(variable, getChild());
    }

    @Override
    protected boolean computeIsDistinct() {
        return getRootNode().isDistinct(this, getChild());
    }

    @Override
    public boolean isDeclaredAsEmpty() {
        return false;
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        IQTree newTree = getRootNode().propagateDownConstraint(constraint, getChild(), variableGenerator);
        return newTree.equals(this) ? this : newTree;
    }

    @Override
    public IQTree replaceSubTree(IQTree subTreeToReplace, IQTree newSubTree) {
        if (equals(subTreeToReplace))
            return newSubTree;

        return iqFactory.createUnaryIQTree(getRootNode(),
                getChild().replaceSubTree(subTreeToReplace, newSubTree));
    }

    @Override
    protected ImmutableSet<Substitution<NonVariableTerm>> computePossibleVariableDefinitions() {
            return getRootNode().getPossibleVariableDefinitions(getChild());
    }

    @Override
    protected ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        return getRootNode().inferUniqueConstraints(getChild());
    }

    @Override
    protected VariableNonRequirement computeVariableNonRequirement() {
        return getRootNode().computeVariableNonRequirement(getChild());
    }

    @Override
    public IQTree removeDistincts() {
        IQTreeCache treeCache = getTreeCache();

        return treeCache.areDistinctAlreadyRemoved()
            ? this
            : getRootNode().removeDistincts(getChild(), treeCache);
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(getChild());
    }

    @Override
    protected VariableNullability computeVariableNullability() {
        return getRootNode().getVariableNullability(getChild());
    }

    @Override
    public IQTree acceptTransformer(IQTreeVisitingTransformer transformer) {
        return getRootNode().acceptTransformer(this, transformer, getChild());
    }

    @Override
    public <T> IQTree acceptTransformer(IQTreeExtendedTransformer<T> transformer, T context) {
        return getRootNode().acceptTransformer(this, transformer, getChild(), context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor) {
        return getRootNode().acceptVisitor(visitor, getChild());
    }

    @Override
    public IQTree getChild() {
        return getChildren().get(0);
    }

    @Override
    protected FunctionalDependencies computeFunctionalDependencies() {
        return getRootNode().inferFunctionalDependencies(getChild(), inferUniqueConstraints(), getVariables());
    }
}
