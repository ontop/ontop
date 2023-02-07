package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
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
            return ImmutableSubstitutionImpl.covariantCast(f);

        if (f.isEmpty())
            return ImmutableSubstitutionImpl.covariantCast(g);

        ImmutableMap<Variable, T> map = Stream.concat(
                        f.entrySet().stream()
                                .map(e -> Maps.immutableEntry(e.getKey(), applyToTerm(g, e.getValue()))),
                        g.entrySet().stream())
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (fValue, gValue) -> fValue));

        return new ImmutableSubstitutionImpl<>(map, termFactory);
    }

    private static Variable applyToVariable(ImmutableSubstitution<Variable> substitution, Variable v) {
        return Optional.ofNullable(substitution.get(v)).orElse(v);
    }

    @Override
    public ImmutableSubstitution<T> rename(InjectiveVar2VarSubstitution renaming, ImmutableSubstitution<? extends T> substitution) {
        if (renaming.isEmpty())
            return ImmutableSubstitutionImpl.covariantCast(substitution);

        ImmutableMap<Variable, T> map = substitution.entrySet().stream()
                // no clashes in new keys because the substitution is injective
                .map(e -> Maps.immutableEntry(applyToVariable(renaming, e.getKey()), rename(renaming, e.getValue())))
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap());

        return new ImmutableSubstitutionImpl<>(map, termFactory);
    }
}
