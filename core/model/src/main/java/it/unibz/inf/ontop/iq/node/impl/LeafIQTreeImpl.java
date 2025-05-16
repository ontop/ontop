package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.impl.AbstractIQTree;
import it.unibz.inf.ontop.iq.impl.DescendingSubstitution;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class LeafIQTreeImpl extends AbstractIQTree implements LeafIQTree {

    protected LeafIQTreeImpl(IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        super(iqTreeTools, iqFactory);
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
        DescendingSubstitution ds = new DescendingSubstitution(descendingSubstitution, getVariables());
        try {
            return ds.normalizeDescendingSubstitution()
                    .map(s -> applyDescendingSubstitutionWithoutOptimizing(s, variableGenerator))
                    .orElse(this);
        }
        catch (DescendingSubstitution.UnsatisfiableDescendingSubstitutionException e) {
            return iqTreeTools.createEmptyNode(ds);
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

    @Override
    public ImmutableSet<Variable> inferStrictDependents() {
        return IQTreeTools.computeStrictDependentsFromFunctionalDependencies(this);
    }
}
