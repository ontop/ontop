package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class LeafIQTreeImpl extends QueryNodeImpl implements LeafIQTree {

    private static final ImmutableList<IQTree> EMPTY_LIST = ImmutableList.of();
    private final IQTreeTools iqTreeTools;
    protected final IntermediateQueryFactory iqFactory;

    protected LeafIQTreeImpl(IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        super();
        this.iqTreeTools = iqTreeTools;
        this.iqFactory = iqFactory;
    }

    @Override
    public LeafIQTree getRootNode() {
        return this;
    }

    @Override
    public ImmutableList<IQTree> getChildren() {
        return EMPTY_LIST;
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        return this;
    }

    @Override
    public boolean isConstructed(Variable variable) {
        return false;
    }

    @Override
    public boolean isEquivalentTo(IQTree tree) {
        return (tree instanceof LeafIQTree)
                && isEquivalentTo((QueryNode) tree);
    }

    /**
     * NB: the constraint is irrelevant here
     */
    @Override
    public final IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint) {
        try {
            return iqTreeTools.normalizeDescendingSubstitution(this, descendingSubstitution)
                    .map(this::applyDescendingSubstitution)
                    .orElse(this);
        } catch (IQTreeTools.UnsatisfiableDescendingSubstitutionException e) {
            return iqFactory.createEmptyNode(getVariables());
        }
    }

    protected abstract IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution);

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint) {
        return this;
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable) {
        return this;
    }
}
