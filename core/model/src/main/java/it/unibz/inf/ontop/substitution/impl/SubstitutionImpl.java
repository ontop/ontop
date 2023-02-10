package it.unibz.inf.ontop.substitution.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionOperations;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Wrapper above an {@code ImmutableMap<Variable, ImmutableTerm>} map.
 */

public class SubstitutionImpl<T extends ImmutableTerm> implements Substitution<T> {

    protected final TermFactory termFactory;
    protected final SubstitutionOperations<ImmutableTerm> defaultOperations;
    protected final ImmutableMap<Variable, T> map;

    public SubstitutionImpl(ImmutableMap<Variable, ? extends T> map, TermFactory termFactory) {
        this.termFactory = termFactory;
        this.defaultOperations = new ImmutableTermsSubstitutionOperations(termFactory);
        //noinspection unchecked
        this.map = (ImmutableMap<Variable, T>) map;

        if (this.map.entrySet().stream().anyMatch(e -> e.getKey().equals(e.getValue())))
            throw new IllegalArgumentException("Please do not insert entries like t/t in your substitution " +
                    "(for efficiency reasons)\n. Substitution: " + this.map);
    }

    static <T extends ImmutableTerm> Substitution<T> covariantCast(Substitution<? extends T> substitution) {
        //noinspection unchecked
        return (Substitution<T>) substitution;
    }

    @Override
    public Stream<Map.Entry<Variable, T>> stream() {
        return map.entrySet().stream();
    }

    @Override
    public  boolean isDefining(Variable variable) {
        return map.containsKey(variable);
    }

    @Override
    public  ImmutableSet<Variable> getDomain() {
        return map.keySet();
    }

    @Override
    public boolean rangeAllMatch(Predicate<T> predicate) {
        return map.values().stream().allMatch(predicate);
    }

    @Override
    public boolean rangeAnyMatch(Predicate<T> predicate) {
        return map.values().stream().anyMatch(predicate);
    }

    @Override
    public ImmutableSet<T> getRangeSet() {
        return ImmutableSet.copyOf(map.values());
    }

    @Override
    public ImmutableSet<Variable> getRangeVariables() {
        return map.values().stream()
                .flatMap(ImmutableTerm::getVariableStream)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public  T get(Variable variable) {
        return map.get(variable);
    }

    @Override
    public  boolean isEmpty() {
        return map.isEmpty();
    }


    @Override
    public SubstitutionOperations<ImmutableTerm> onImmutableTerms() {
        return defaultOperations;
    }


    @Override
    public boolean isInjective() {
        return InjectiveSubstitutionImpl.isInjective(map);
    }

    @Override
    public ImmutableMap<T, Collection<Variable>> inverseMap() {
       return map.entrySet().stream()
               .collect(ImmutableCollectors.toMultimap(Map.Entry::getValue, Map.Entry::getKey))
               .asMap();
    }

    @Override
    public Substitution<T> restrictDomainTo(Set<Variable> set) {
        return new SubstitutionImpl<>(map.entrySet().stream()
                .filter(e -> set.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap()), termFactory);
    }

    @Override
    public Substitution<T> removeFromDomain(Set<Variable> set) {
        return new SubstitutionImpl<>(map.entrySet().stream()
                .filter(e -> !set.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap()), termFactory);
    }

    @Override
    public <S extends ImmutableTerm> Substitution<S> restrictRangeTo(Class<? extends S> type) {
        return new SubstitutionImpl<>(map.entrySet().stream()
                .filter(e -> type.isInstance(e.getValue()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> type.cast(e.getValue()))), termFactory);
    }

    @Override
    public ImmutableSet<Variable> getPreImage(Predicate<T> predicate) {
        return map.entrySet().stream()
                .filter(e -> predicate.test(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public <S extends ImmutableTerm> Substitution<S> castTo(Class<S> type) {
        if (map.entrySet().stream().anyMatch(e -> !type.isInstance(e.getValue())))
            throw new ClassCastException();

        //noinspection unchecked
        return (Substitution<S>) this;
    }

    @Override
    public <S extends ImmutableTerm> Substitution<S> transform(Function<T, S> function) {
        return new SubstitutionImpl<>(map.entrySet().stream()
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> function.apply(e.getValue()))), termFactory);
    }

    @Override
    public InjectiveSubstitution<T> injective() {
        return new InjectiveSubstitutionImpl<>(map, termFactory);
    }

    @Override
    public Builder<T> builder() {
        return new BuilderImpl<>(map.entrySet().stream(), termFactory);
    }

    protected static class BuilderImpl<B extends ImmutableTerm> implements Builder<B> {
        protected final Stream<Map.Entry<Variable, B>> stream;
        protected final TermFactory termFactory;

        BuilderImpl(Stream<Map.Entry<Variable, B>> stream, TermFactory termFactory) {
            this.stream = stream;
            this.termFactory = termFactory;
        }
        @Override
        public Substitution<B> build() {
            return new SubstitutionImpl<>(stream.collect(ImmutableCollectors.toMap()), termFactory);
        }

        @Override
        public <S extends ImmutableTerm> Substitution<S> build(Class<S> type) {
            ImmutableMap<Variable, B> map = stream.collect(ImmutableCollectors.toMap());
            if (map.entrySet().stream().anyMatch(e -> !type.isInstance(e.getValue())))
                throw new ClassCastException();

            //noinspection unchecked
            return new SubstitutionImpl<>((ImmutableMap<Variable, S>)map, termFactory);
        }

        protected <S extends ImmutableTerm> Builder<S> create(Stream<Map.Entry<Variable, S>> stream) {
            return new BuilderImpl<>(stream, termFactory);
        }

        private Builder<B> restrictDomain(Predicate<Variable> predicate) {
            return create(stream.filter(e -> predicate.test(e.getKey())));
        }

        @Override
        public Builder<B> restrictDomainTo(Set<Variable> set) {
            return restrictDomain(set::contains);
        }

        @Override
        public Builder<B> removeFromDomain(Set<Variable> set) {
            return restrictDomain(v -> !set.contains(v));
        }

        @Override
        public Builder<B> restrict(BiPredicate<Variable, B> predicate) {
            return create(stream.filter(e -> predicate.test(e.getKey(), e.getValue())));
        }

        @Override
        public Builder<B> restrictRange(Predicate<B> predicate) {
            return create(stream.filter(e -> predicate.test(e.getValue())));
        }

        @Override
        public <S extends ImmutableTerm> Builder<S> restrictRangeTo(Class<? extends S> type) {
            return create(stream
                    .filter(e -> type.isInstance(e.getValue()))
                    .map(e -> Maps.immutableEntry(e.getKey(), type.cast(e.getValue()))));
        }

        @Override
        public <U, S extends ImmutableTerm> Builder<S> transform(Function<Variable, U> lookup, BiFunction<B, U, S> function) {
            return create(stream.map(e -> Maps.immutableEntry(e.getKey(), function.apply(e.getValue(), lookup.apply(e.getKey())))));
        }

        @Override
        public <S extends ImmutableTerm> Builder<S> transform(Function<B, S> function) {
            return create(stream.map(e -> Maps.immutableEntry(e.getKey(), function.apply(e.getValue()))));
        }

        @Override
        public <U> Builder<B> transformOrRetain(Function<Variable, U> lookup, BiFunction<B, U, B> function) {
            return create(stream.map(e -> Maps.immutableEntry(
                    e.getKey(),
                    Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(u -> function.apply(e.getValue(), u))
                            .orElse(e.getValue()))));
        }

        @Override
        public <U, S extends ImmutableTerm> Builder<S> transformOrRemove(Function<Variable, U> lookup, Function<U, S> function) {
            return create(stream
                    .flatMap(e -> Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(function)
                            .map(r -> Maps.immutableEntry(e.getKey(), r)).stream()));
        }

        @Override
        public <U> Builder<B> flatTransform(Function<Variable, U> lookup, Function<U, Substitution<B>> function) {
            return create(stream
                    .flatMap(e -> Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(function)
                            .map(Substitution::stream)
                            .orElseGet(() -> Stream.of(e))));
        }

        @Override
        public Stream<ImmutableExpression> toStrictEqualities() {
            return toStream(termFactory::getStrictEquality);
        }

        @Override
        public <S> Stream<S> toStream(BiFunction<Variable, B, S> transformer) {
            return stream.map(e -> transformer.apply(e.getKey(), e.getValue()));
        }

        @Override
        public <S> ImmutableMap<Variable, S> toMap(BiFunction<Variable, B, S> transformer) {
            return stream.collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> transformer.apply(e.getKey(), e.getValue())));
        }

        @Override
        public <S> ImmutableMap<Variable, S> toMapWithoutOptional(BiFunction<Variable, B, Optional<S>> transformer) {
            return stream
                    .flatMap(e -> transformer.apply(e.getKey(), e.getValue()).stream()
                            .map(v -> Maps.immutableEntry(e.getKey(), v)))
                    .collect(ImmutableCollectors.toMap());
        }
    }


    ImmutableMap<Variable, T> getImmutableMap() {
        return map;
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator("/").join(map);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SubstitutionImpl) {
            return map.equals(((SubstitutionImpl<?>) other).map);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
