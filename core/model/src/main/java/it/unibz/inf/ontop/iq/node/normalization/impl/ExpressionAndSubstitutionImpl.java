package it.unibz.inf.ontop.iq.node.normalization.impl;

import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonFunctionalTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public class ExpressionAndSubstitutionImpl implements ConditionSimplifier.ExpressionAndSubstitution {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<ImmutableExpression> optionalExpression;
    private final ImmutableSubstitution<NonFunctionalTerm> substitution;

    public ExpressionAndSubstitutionImpl(Optional<ImmutableExpression> optionalExpression,
                                         ImmutableSubstitution<NonFunctionalTerm> substitution) {
        this.optionalExpression = optionalExpression;
        this.substitution = substitution;
    }

    @Override
    public ImmutableSubstitution<NonFunctionalTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalExpression() {
        return optionalExpression;
    }
}
