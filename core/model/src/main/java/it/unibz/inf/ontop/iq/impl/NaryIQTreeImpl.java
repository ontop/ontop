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
    protected IQTree doNormalizeForOptimization(VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return getRootNode().normalizeForOptimization(getChildren(), variableGenerator, treeCache);
    }

    @Override
    protected void validateNode() throws InvalidIntermediateQueryException {
        getRootNode().validateNode(getChildren());
    }

    @Override
    protected VariableNullability computeVariableNullability() {
        return getRootNode().getVariableNullability(getChildren());
    }



    /**
     * TODO: use the properties for optimization purposes?
     */
    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return getRootNode().liftIncompatibleDefinitions(variable, getChildren(), variableGenerator);
    }

    @Override
    protected IQTree applyNonEmptyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution) {
        return getRootNode().applyFreshRenaming(renamingSubstitution, getChildren(), getTreeCache());
    }

    @Override
    protected IQTree applyRegularDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, getChildren(), variableGenerator);
    }

    @Override
    protected IQTree doApplyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            VariableGenerator variableGenerator) {
            return getRootNode().applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, getChildren(), variableGenerator);
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
    protected IQTree doPropagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        return getRootNode().propagateDownConstraint(constraint, getChildren(), variableGenerator);
    }

    @Override
    protected IQTree createIQTree(ImmutableList<IQTree> newChildren) {
        return iqFactory.createNaryIQTree(getRootNode(), newChildren);
    }

    @Override
    protected ImmutableSet<Substitution<NonVariableTerm>> computePossibleVariableDefinitions() {
        return getRootNode().getPossibleVariableDefinitions(getChildren());
    }

    @Override
    protected IQTree doRemoveDistincts(IQTreeCache treeCache) {
        return getRootNode().removeDistincts(getChildren(), treeCache);
    }

    @Override
    protected ImmutableSet<ImmutableSet<Variable>> computeUniqueConstraints() {
        return getRootNode().inferUniqueConstraints(getChildren());
    }

    @Override
    protected ImmutableSet<Variable> computeStrictDependents() {
        return getRootNode().inferStrictDependents(this, getChildren());
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
