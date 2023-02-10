package it.unibz.inf.ontop.substitution.impl;


import com.google.common.collect.*;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

import java.util.Set;
import java.util.function.Function;

public class InjectiveSubstitutionImpl<T extends ImmutableTerm> extends SubstitutionImpl<T> implements InjectiveSubstitution<T> {

    InjectiveSubstitutionImpl(ImmutableMap<Variable, T> map, TermFactory termFactory) {
        super(map, termFactory);

        if (!isInjective(this.map))
            throw new IllegalArgumentException("Non-injective map given: " + map);
    }

    @Override
    public String toString() {
        return "injective " + super.toString();
    }

    @Override
    protected  <S extends ImmutableTerm> InjectiveSubstitution<S> createSubstitution(ImmutableMap<Variable, S> newMap) {
        return new InjectiveSubstitutionImpl<>(newMap, termFactory);
    }

    @Override
    public InjectiveSubstitution<T> restrictDomainTo(Set<Variable> set) {
        return (InjectiveSubstitution<T>)super.restrictDomainTo(set);
    }

    @Override
    public InjectiveSubstitution<T> removeFromDomain(Set<Variable> set) {
        return (InjectiveSubstitution<T>)super.removeFromDomain(set);
    }

    @Override
    public <S extends ImmutableTerm> InjectiveSubstitution<S> restrictRangeTo(Class<? extends S> type) {
        return (InjectiveSubstitution<S>)super.<S>restrictRangeTo(type);
    }

    @Override
    public <S extends ImmutableTerm> InjectiveSubstitution<S> transform(Function<T, S> function) {
        return (InjectiveSubstitution<S>)super.transform(function);
    }


    static <T> boolean isInjective(ImmutableMap<Variable, T> map) {
        ImmutableCollection<T> values = map.values();
        return values.size() == ImmutableSet.copyOf(values).size();
    }
}
