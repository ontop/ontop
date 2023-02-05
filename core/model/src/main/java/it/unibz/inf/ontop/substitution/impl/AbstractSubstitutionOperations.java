package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionOperations;
import it.unibz.inf.ontop.substitution.UnifierBuilder;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractSubstitutionOperations<T extends ImmutableTerm> extends AbstractSubstitutionComposition<T> implements SubstitutionOperations<T> {

    AbstractSubstitutionOperations(TermFactory termFactory) {
        super(termFactory);
    }

    @Override
    public ImmutableFunctionalTerm apply(ImmutableSubstitution<? extends T> substitution, ImmutableFunctionalTerm term) {
        if (term.getFunctionSymbol() instanceof BooleanFunctionSymbol)
            return apply(substitution, (ImmutableExpression)term);

        if (substitution.isEmpty())
            return term;

        return termFactory.getImmutableFunctionalTerm(term.getFunctionSymbol(), substitution.applyToTerms(term.getTerms()));
    }

    @Override
    public ImmutableExpression apply(ImmutableSubstitution<? extends T> substitution, ImmutableExpression expression) {
        if (substitution.isEmpty())
            return expression;

        return termFactory.getImmutableExpression(expression.getFunctionSymbol(), substitution.applyToTerms(expression.getTerms()));
    }

    @Override
    public ImmutableList<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends Variable> variables) {
        return variables.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableSet<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableSet<? extends Variable> terms) {
        return terms.stream()
                .map(v -> apply(substitution, v))
                .collect(ImmutableCollectors.toSet());
    }


    @Override
    public UnifierBuilder<T> unifierBuilder() {
        return unifierBuilder(termFactory.getSubstitution(ImmutableMap.of()));
    }
}
