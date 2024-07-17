package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class LeafIQTreeImpl extends QueryNodeImpl implements LeafIQTree {

    protected final IQTreeTools iqTreeTools;

    protected LeafIQTreeImpl(IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        super(iqFactory);
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public LeafIQTree getRootNode() {
        return this;
    }

    @Override
    public ImmutableList<IQTree> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public IQTree normalizeForOptimization(VariableGenerator variableGenerator) {
        return this;
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return false;
    }

    /**
     * NB: the constraint is irrelevant here
     */
    @Override
    public final IQTree applyDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint,
            VariableGenerator variableGenerator) {
        try {
            return iqTreeTools.normalizeDescendingSubstitution(this, descendingSubstitution)
                    .map(s -> applyDescendingSubstitutionWithoutOptimizing(s, variableGenerator))
                    .orElse(this);
        }
        catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(iqTreeTools.computeNewProjectedVariables(descendingSubstitution, getVariables()));
        }
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        return this;
    }

    @Override
    public IQTree replaceSubTree(IQTree subTreeToReplace, IQTree newSubTree) {
        return equals(subTreeToReplace)
                ? newSubTree
                : this;
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        return ImmutableSet.of(iqTreeTools.getEmptyNonVariableSubstitution());
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, VariableGenerator variableGenerator) {
        return this;
    }

    @Override
    public IQTree removeDistincts() {
        return this;
    }
}
