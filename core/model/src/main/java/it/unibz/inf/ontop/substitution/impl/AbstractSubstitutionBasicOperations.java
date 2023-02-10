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
import java.util.stream.Stream;
import java.util.function.Function;

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
                        f.stream().map(e -> Maps.immutableEntry(e.getKey(), applyToTerm(g, e.getValue()))),
                        g.stream())
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (fValue, gValue) -> fValue));

        return new SubstitutionImpl<>(map, termFactory);
    }

    /**
     *  Applies the substitution to the variable.
     *
     * @param substitution the substitution
     * @param v the variable
     * @param typeCast effectively ensures that Variable is a subtype of T (normally, is simply v -> v)
     */

    protected static <T extends ImmutableTerm> T applyToVariable(Substitution<? extends T> substitution, Variable v, Function<Variable, T> typeCast) {
        T t = substitution.get(v);
        if (t != null)
            return t;

        return typeCast.apply(v);
    }

    @Override
    public Substitution<T> rename(InjectiveSubstitution<Variable> renaming, Substitution<? extends T> substitution) {
        if (renaming.isEmpty())
            return SubstitutionImpl.covariantCast(substitution);

        ImmutableMap<Variable, T> map = substitution.stream()
                // no clashes in new keys because the substitution is injective
                .map(e -> Maps.immutableEntry(applyToVariable(renaming, e.getKey(), v -> v), rename(renaming, e.getValue())))
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap());

        return new SubstitutionImpl<>(map, termFactory);
    }
}
