package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class LimitOrOffsetNodeImpl extends QueryModifierNodeImpl {

    protected LimitOrOffsetNodeImpl(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, IQTree child) {
        return iqFactory.createUnaryIQTree(this,
                child.applyDescendingSubstitution(descendingSubstitution, constraint));
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return ImmutableSet.of();
    }
}
