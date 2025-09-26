package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.impl.AbstractIQTree;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class LeafIQTreeImpl extends AbstractIQTree implements LeafIQTree, QueryNode {

    protected final SubstitutionFactory substitutionFactory;
    protected final CoreUtilsFactory coreUtilsFactory;

    protected LeafIQTreeImpl(IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory, CoreUtilsFactory coreUtilsFactory) {
        super(iqTreeTools, iqFactory);
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return getVariables();
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        return getVariables();
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
        DownPropagation ds = new DownPropagation(descendingSubstitution, getVariables());
        try {
            return ds.applyNormalizedDescendingSubstitution(this,
                            (t, s) -> applyDescendingSubstitutionWithoutOptimizing(s, variableGenerator));
        }
        catch (DownPropagation.UnsatisfiableDescendingSubstitutionException e) {
            return iqTreeTools.createEmptyNode(ds);
        }
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, VariableGenerator variableGenerator) {
        return this;
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions() {
        return ImmutableSet.of(substitutionFactory.getSubstitution());
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
