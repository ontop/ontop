package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionBasicOperations;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractSubstitutionBasicOperations<T extends ImmutableTerm> implements SubstitutionBasicOperations<T> {

    protected final TermFactory termFactory;

    AbstractSubstitutionBasicOperations(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override
    public ImmutableList<T> applyToTerms(Substitution<? extends T> substitution, ImmutableList<? extends T> terms) {
        return terms.stream()
                .map(t -> applyToTerm(substitution, t))
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public ImmutableMap<Integer, T> applyToTerms(Substitution<? extends T> substitution, ImmutableMap<Integer, ? extends T> argumentMap) {
        return argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> applyToTerm(substitution, e.getValue())));
    }

    @Override
    public Substitution<T> compose(Substitution<? extends T> g, Substitution<? extends T> f) {
        if (g.isEmpty())
            return SubstitutionImpl.covariantCast(f);

        if (f.isEmpty())
            return SubstitutionImpl.covariantCast(g);

        ImmutableMap<Variable, T> map = Stream.concat(
                        f.entrySet().stream()
                                .map(e -> Maps.immutableEntry(e.getKey(), applyToTerm(g, e.getValue()))),
                        g.entrySet().stream())
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (fValue, gValue) -> fValue));

        return new SubstitutionImpl<>(map, termFactory);
    }

    private static Variable applyToVariable(Substitution<Variable> substitution, Variable v) {
        return Optional.ofNullable(substitution.get(v)).orElse(v);
    }

    @Override
    public Substitution<T> rename(InjectiveSubstitution<Variable> renaming, Substitution<? extends T> substitution) {
        if (renaming.isEmpty())
            return SubstitutionImpl.covariantCast(substitution);

        ImmutableMap<Variable, T> map = substitution.entrySet().stream()
                // no clashes in new keys because the substitution is injective
                .map(e -> Maps.immutableEntry(applyToVariable(renaming, e.getKey()), rename(renaming, e.getValue())))
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap());

        return new SubstitutionImpl<>(map, termFactory);
    }
}
