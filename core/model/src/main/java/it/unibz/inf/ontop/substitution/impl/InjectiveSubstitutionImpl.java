package it.unibz.inf.ontop.substitution.impl;


import com.google.common.collect.*;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class InjectiveSubstitutionImpl<T extends ImmutableTerm> extends SubstitutionImpl<T> implements InjectiveSubstitution<T> {

    InjectiveSubstitutionImpl(ImmutableMap<Variable, T> map, TermFactory termFactory, boolean checkEntries) {
        super(map, termFactory, checkEntries);

        if (checkEntries && !isInjective(this.map))
            throw new IllegalArgumentException("Non-injective map given: " + map);
    }

    @Override
    public String toString() {
        return "injective " + super.toString();
    }

    @Override
    protected  <T extends ImmutableTerm, S extends ImmutableTerm> InjectiveSubstitution<S> createSubstitution(Stream<Map.Entry<Variable, T>> stream, Function<Map.Entry<Variable, T>, S> mapper, boolean checkEntries) {
        return new InjectiveSubstitutionImpl<>(stream.collect(ImmutableCollectors.toMap(Map.Entry::getKey, mapper)), termFactory, checkEntries);
    }

    @Override
    public InjectiveSubstitution<T> restrictDomainTo(Set<Variable> set) {
        return createSubstitution(stream().filter(e -> set.contains(e.getKey())), Map.Entry::getValue, false);
    }

    @Override
    public InjectiveSubstitution<T> removeFromDomain(Set<Variable> set) {
        return createSubstitution(stream().filter(e -> !set.contains(e.getKey())), Map.Entry::getValue, false);
    }

    @Override
    public <S extends ImmutableTerm> InjectiveSubstitution<S> restrictRangeTo(Class<? extends S> type) {
        return createSubstitution(stream().filter(e -> type.isInstance(e.getValue())), e -> type.cast(e.getValue()), false);
    }

    @Override
    public <S extends ImmutableTerm> InjectiveSubstitution<S> transform(Function<T, S> function) {
        return createSubstitution(stream(), e -> function.apply(e.getValue()), true);
    }


    static <T> boolean isInjective(ImmutableMap<Variable, T> map) {
        ImmutableCollection<T> values = map.values();
        return values.size() == ImmutableSet.copyOf(values).size();
    }


    @Override
    public InjectiveSubstitution.Builder<T, ? extends InjectiveSubstitution.Builder<T, ?>> builder() {
        return new BuilderImpl<>(map.entrySet().stream(), false);
    }

    protected class BuilderImpl<BT extends ImmutableTerm, B extends InjectiveSubstitution.Builder<BT, ? extends B>>
            extends SubstitutionImpl<T>.AbstractBuilderImpl<BT,  BuilderImpl<BT, B>>
            implements InjectiveSubstitution.Builder<BT, BuilderImpl<BT, B>> {

        BuilderImpl(Stream<Map.Entry<Variable, BT>> stream, boolean checkEntries) {
            super(stream, checkEntries);
        }

        @Override
        protected BuilderImpl<BT, B> createBuilder(Stream<Map.Entry<Variable, BT>> stream, boolean checkEntries) {
            return new BuilderImpl<>(stream, checkEntries);
        }

        @Override
        public InjectiveSubstitution<BT> build() { return createSubstitution(stream, Map.Entry::getValue, checkEntries); }
    }

}
