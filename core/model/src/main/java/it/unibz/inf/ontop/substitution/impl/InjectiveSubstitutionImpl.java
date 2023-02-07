package it.unibz.inf.ontop.substitution.impl;


import com.google.common.collect.*;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Set;

public class InjectiveSubstitutionImpl<T extends ImmutableTerm> extends SubstitutionImpl<T> implements InjectiveSubstitution<T> {

    protected InjectiveSubstitutionImpl(ImmutableMap<Variable, T> map, TermFactory termFactory) {
        super(map, termFactory);

        if (!isInjective(this.map))
            throw new IllegalArgumentException("Non-injective map given: " + map);
    }

    @Override
    public String toString() {
        return "injective " + super.toString();
    }


    @Override
    public InjectiveSubstitution<T> restrictDomainTo(Set<Variable> set) {
        return new InjectiveSubstitutionImpl<>(map.entrySet().stream()
                .filter(e -> set.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap()), termFactory);
    }

    @Override
    public InjectiveSubstitution<T> removeFromDomain(Set<Variable> set) {
        return new InjectiveSubstitutionImpl<>(map.entrySet().stream()
                .filter(e -> !set.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap()), termFactory);
    }


    static <T> boolean isInjective(ImmutableMap<Variable, T> map) {
        ImmutableCollection<T> values = map.values();
        return values.size() == ImmutableSet.copyOf(values).size();
    }

}
