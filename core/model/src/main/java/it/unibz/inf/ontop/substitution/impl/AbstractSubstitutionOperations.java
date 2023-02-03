package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionOperations;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractSubstitutionOperations<T extends ImmutableTerm> implements SubstitutionOperations<T> {

    protected final TermFactory termFactory;

    AbstractSubstitutionOperations(TermFactory termFactory) {
        this.termFactory = termFactory;
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
    public ImmutableList<T> applyToTerms(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends T> terms) {
        return terms.stream()
                .map(t -> applyToTerm(substitution, t))
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableMap<Integer, T> applyToTerms(ImmutableSubstitution<? extends T> substitution, ImmutableMap<Integer, ? extends T> argumentMap) {
        return argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> applyToTerm(substitution, e.getValue())));
    }

    @Override
    public ImmutableSubstitution<T> compose(ImmutableSubstitution<? extends T> g, ImmutableSubstitution<? extends T> f) {
        if (g.isEmpty())
            return ImmutableSubstitutionImpl.invariantCast(f);

        if (f.isEmpty())
            return ImmutableSubstitutionImpl.invariantCast(g);

        ImmutableMap<Variable, T> map = Stream.concat(
                        f.entrySet().stream()
                                .map(e -> Maps.immutableEntry(e.getKey(), applyToTerm(g, e.getValue()))),
                        g.entrySet().stream())
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (fValue, gValue) -> fValue));

        return new ImmutableSubstitutionImpl<>(map, termFactory);
    }

    @Override
    public ImmutableUnificationTools.UnifierBuilder<T, ?> unifierBuilder() {
        return unifierBuilder(termFactory.getSubstitution(ImmutableMap.of()));
    }
}
