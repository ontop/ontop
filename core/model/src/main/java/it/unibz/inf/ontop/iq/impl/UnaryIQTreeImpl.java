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
    public IQTree getChild() {
        return getChildren().get(0);
    }

    @Override
    protected IQTree doNormalizeForOptimization(VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return getRootNode().normalizeForOptimization(getChild(), variableGenerator, treeCache);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return getRootNode().liftIncompatibleDefinitions(variable, getChild(), variableGenerator);
    }

    @Override
    protected IQTree applyNonEmptyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution) {
        return getRootNode().applyFreshRenaming(renamingSubstitution, getChild(), getTreeCache());
    }

    @Override
    protected IQTree applyRegularDescendingSubstitution(Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                                        Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator) {
        return getRootNode().applyDescendingSubstitution(descendingSubstitution, constraint, getChild(), variableGenerator);
    }

    @Override
    protected IQTree doApplyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, VariableGenerator variableGenerator) {
        return getRootNode().applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, getChild(), variableGenerator);
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return getVariables().contains(variable) && getRootNode().isConstructed(variable, getChild());
    }

    @Override
    protected boolean computeIsDistinct() {
        return getRootNode().isDistinct(this, getChild());
    }

    @Override
    protected IQTree doPropagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        return getRootNode().propagateDownConstraint(constraint, getChild(), variableGenerator);
    }

    @Override
    protected IQTree createIQTree(ImmutableList<IQTree> newChildren) {
        return iqFactory.createUnaryIQTree(getRootNode(), newChildren.get(0));
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
    protected ImmutableSet<Variable> computeStrictDependents() {
        return getRootNode().inferStrictDependents(this, getChild());
    }

    @Override
    protected VariableNonRequirement computeVariableNonRequirement() {
        return getRootNode().computeVariableNonRequirement(getChild());
    }

    @Override
    protected IQTree doRemoveDistincts(IQTreeCache treeCache) {
        return getRootNode().removeDistincts(getChild(), treeCache);
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
    protected FunctionalDependencies computeFunctionalDependencies() {
        return getRootNode().inferFunctionalDependencies(getChild(), inferUniqueConstraints(), getVariables());
    }
}
