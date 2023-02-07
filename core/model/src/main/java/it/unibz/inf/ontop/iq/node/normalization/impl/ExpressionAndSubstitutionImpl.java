package it.unibz.inf.ontop.iq.node.normalization.impl;

import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.Optional;

public class ExpressionAndSubstitutionImpl implements ConditionSimplifier.ExpressionAndSubstitution {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<ImmutableExpression> optionalExpression;
    private final Substitution<VariableOrGroundTerm> substitution;

    public ExpressionAndSubstitutionImpl(Optional<ImmutableExpression> optionalExpression,
                                         Substitution<? extends VariableOrGroundTerm> substitution) {
        this.optionalExpression = optionalExpression;
        this.substitution = substitution.castTo(VariableOrGroundTerm.class);
    }

    @Override
    public Substitution<VariableOrGroundTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalExpression() {
        return optionalExpression;
    }
}
