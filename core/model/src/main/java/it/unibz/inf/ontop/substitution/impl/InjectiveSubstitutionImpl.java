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


    @Override
    public InjectiveSubstitution.Builder<T, ? extends InjectiveSubstitution.Builder<T, ?>> builder() {
        return new BuilderImpl<>(map.entrySet().stream());
    }

    protected class BuilderImpl<BT extends ImmutableTerm, B extends InjectiveSubstitution.Builder<BT, ? extends B>>
            extends SubstitutionImpl<T>.AbstractBuilderImpl<BT,  BuilderImpl<BT, B>>
            implements InjectiveSubstitution.Builder<BT, BuilderImpl<BT, B>> {

        BuilderImpl(Stream<Map.Entry<Variable, BT>> stream) {
            super(stream);
        }

        @Override
        protected BuilderImpl<BT, B> createBuilder(Stream<Map.Entry<Variable, BT>> stream) {
            return new BuilderImpl<>(stream);
        }

        @Override
        public InjectiveSubstitution<BT> build() { return createSubstitution(stream.collect(ImmutableCollectors.toMap())); }
    }

}
