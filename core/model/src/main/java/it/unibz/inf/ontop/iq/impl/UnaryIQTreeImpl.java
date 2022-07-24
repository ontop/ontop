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
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public class UnaryIQTreeImpl extends AbstractCompositeIQTree<UnaryOperatorNode> implements UnaryIQTree {

    @AssistedInject
    private UnaryIQTreeImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQTree child,
                            @Assisted IQTreeCache treeCache, IQTreeTools iqTreeTools,
                            IntermediateQueryFactory iqFactory, TermFactory termFactory, OntopModelSettings settings) {
        super(rootNode, ImmutableList.of(child), treeCache, iqTreeTools, iqFactory, termFactory);

        if (settings.isTestModeEnabled())
            validate();
    }


    @AssistedInject
    private UnaryIQTreeImpl(@Assisted UnaryOperatorNode rootNode, @Assisted IQTree child, IQTreeTools iqTreeTools,
                            IntermediateQueryFactory iqFactory, TermFactory termFactory, OntopModelSettings settings,
                            IQTreeCache freshTreeCache) {
        this(rootNode, child, freshTreeCache, iqTreeTools, iqFactory, termFactory, settings);
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
    protected IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, boolean alreadyNormalized) {
        InjectiveVar2VarSubstitution selectedSubstitution = alreadyNormalized
                ? renamingSubstitution
                : renamingSubstitution.filter(getVariables()::contains);

        return selectedSubstitution.isEmpty()
                ? this
                : getRootNode().applyFreshRenaming(renamingSubstitution, getChild(), getTreeCache());
    }

    @Override
    protected IQTree applyRegularDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                        Optional<ImmutableExpression> constraint) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, getChild());
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {
        try {
            return normalizeDescendingSubstitution(descendingSubstitution)
                    .map(s -> getRootNode().applyDescendingSubstitutionWithoutOptimizing(s, getChild()))
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
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        IQTree newTree = getRootNode().propagateDownConstraint(constraint, getChild());
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
    protected ImmutableSet<ImmutableSubstitution<NonVariableTerm>> computePossibleVariableDefinitions() {
            return getRootNode().getPossibleVariableDefinitions(getChild());
    }

    @Override
    protected ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        return getRootNode().inferUniqueConstraints(getChild());
    }

    @Override
    protected ImmutableSet<Variable> computeNotInternallyRequiredVariables() {
        return getRootNode().computeNotInternallyRequiredVariables(getChild());
    }

    @Override
    public IQTree removeDistincts() {
        IQTreeCache treeCache = getTreeCache();

        return treeCache.areDistinctAlreadyRemoved()
            ? this
            : getRootNode().removeDistincts(getChild(), treeCache);
    }

    @Override
    protected ImmutableSet<Variable> computeVariables() {
        UnaryOperatorNode rootNode = getRootNode();
        if (rootNode instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode) rootNode).getVariables();
        else
            return getChild().getVariables();
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
}
