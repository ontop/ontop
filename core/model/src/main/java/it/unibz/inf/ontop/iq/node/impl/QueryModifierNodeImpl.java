package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.QueryModifierNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public abstract class QueryModifierNodeImpl implements QueryModifierNode, QueryNode {

    protected final IntermediateQueryFactory iqFactory;
    protected final TermFactory termFactory;

    protected QueryModifierNodeImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        return child.getVariableNullability();
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return child.isConstructed(variable);
    }

    @Override
    public IQTree propagateDownConstraint(DownPropagation dp, IQTree child) {
        return iqFactory.createUnaryIQTree(this, dp.propagateToChild(child));
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        return child.getPossibleVariableDefinitions();
    }
}
