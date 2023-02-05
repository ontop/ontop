package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionComposition;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractSubstitutionComposition<T extends ImmutableTerm> implements SubstitutionComposition<T> {

    protected final TermFactory termFactory;

    AbstractSubstitutionComposition(TermFactory termFactory) {
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
}
