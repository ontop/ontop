package it.unibz.inf.ontop.iq.node.normalization.impl;

import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public class ExpressionAndSubstitutionImpl implements ConditionSimplifier.ExpressionAndSubstitution {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<ImmutableExpression> optionalExpression;
    private final ImmutableSubstitution<VariableOrGroundTerm> substitution;

    public ExpressionAndSubstitutionImpl(Optional<ImmutableExpression> optionalExpression,
                                         ImmutableSubstitution<? extends VariableOrGroundTerm> substitution) {
        this.optionalExpression = optionalExpression;
        this.substitution = (ImmutableSubstitution<VariableOrGroundTerm>) substitution;
    }

    @Override
    public ImmutableSubstitution<VariableOrGroundTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalExpression() {
        return optionalExpression;
    }
}
