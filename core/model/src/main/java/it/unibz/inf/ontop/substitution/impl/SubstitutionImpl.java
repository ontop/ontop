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

    public SubstitutionImpl(ImmutableMap<Variable, ? extends T> map,  TermFactory termFactory) {
        this.termFactory = termFactory;
        this.defaultOperations = new ImmutableTermsSubstitutionOperations(termFactory);
        //noinspection unchecked
        this.map = (ImmutableMap<Variable, T>) map;

        if (this.map.entrySet().stream().anyMatch(e -> e.getKey().equals(e.getValue())))
            throw new IllegalArgumentException("Please do not insert entries like t/t in your substitution " +
                    "(for efficiency reasons)\n. Substitution: " + this.map);
    }

    protected  <S extends ImmutableTerm> Substitution<S> createSubstitution(ImmutableMap<Variable, S> newMap) {
        return termFactory.getSubstitution(newMap);
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
        return createSubstitution(map.entrySet().stream()
                .filter(e -> set.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap()));
    }

    @Override
    public Substitution<T> removeFromDomain(Set<Variable> set) {
        return createSubstitution(map.entrySet().stream()
                .filter(e -> !set.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap()));
    }

    @Override
    public <S extends ImmutableTerm> Substitution<S> restrictRangeTo(Class<? extends S> type) {
        return createSubstitution(map.entrySet().stream()
                .filter(e -> type.isInstance(e.getValue()))
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> type.cast(e.getValue()))));
    }

    @Override
    public ImmutableSet<Variable> getPreImage(Predicate<T> predicate) {
        return map.entrySet().stream()
                .filter(e -> predicate.test(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public <S extends ImmutableTerm> Substitution<S> transform(Function<T, S> function) {
        return createSubstitution(map.entrySet().stream()
                .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> function.apply(e.getValue()))));
    }

    @Override
    public InjectiveSubstitution<T> injective() {
        return new InjectiveSubstitutionImpl<>(map, termFactory);
    }

    @Override
    public Builder<T, ? extends Builder<T, ?>> builder() {
        return new BuilderImpl<>(map.entrySet().stream());
    }


    protected class BuilderImpl<BT extends ImmutableTerm> extends AbstractBuilderImpl<BT, BuilderImpl<BT>> {
        BuilderImpl(Stream<Map.Entry<Variable, BT>> stream) {
            super(stream);
        }
        @Override
        protected BuilderImpl<BT> createBuilder(Stream<Map.Entry<Variable, BT>> stream) {
            return new BuilderImpl<>(stream);
        }
        @Override
        public Substitution<BT> build() {
            return createSubstitution(stream.collect(ImmutableCollectors.toMap()));
        }
    }

    protected abstract class AbstractBuilderImpl<BT extends ImmutableTerm, B extends Builder<BT, ? extends B>> implements Builder<BT, B> {
        protected final Stream<Map.Entry<Variable, BT>> stream;

        AbstractBuilderImpl(Stream<Map.Entry<Variable, BT>> stream) {
            this.stream = stream;
        }

        private  <S extends ImmutableTerm> BuilderImpl<S> createBasicBuilder(Stream<Map.Entry<Variable, S>> stream) {
            return new BuilderImpl<>(stream);
        }

        protected abstract B createBuilder(Stream<Map.Entry<Variable, BT>> stream);

        @Override
        public B restrictDomainTo(Set<Variable> set) {
            return createBuilder(stream.filter(e -> set.contains(e.getKey())));
        }

        @Override
        public B removeFromDomain(Set<Variable> set) {
            return createBuilder(stream.filter(e -> !set.contains(e.getKey())));
        }

        @Override
        public B restrict(BiPredicate<Variable, BT> predicate) {
            return createBuilder(stream.filter(e -> predicate.test(e.getKey(), e.getValue())));
        }

        @Override
        public B restrictRange(Predicate<BT> predicate) {
            return createBuilder(stream.filter(e -> predicate.test(e.getValue())));
        }

        @Override
        public <S extends ImmutableTerm> Builder<S, ?> restrictRangeTo(Class<? extends S> type) {
            return createBasicBuilder(stream
                    .filter(e -> type.isInstance(e.getValue()))
                    .map(e -> Maps.immutableEntry(e.getKey(), type.cast(e.getValue()))));
        }

        @Override
        public <U, S extends ImmutableTerm> Builder<S, ?> transform(Function<Variable, U> lookup, BiFunction<BT, U, S> function) {
            return createBasicBuilder(stream.map(e -> Maps.immutableEntry(e.getKey(), function.apply(e.getValue(), lookup.apply(e.getKey())))));
        }

        @Override
        public <S extends ImmutableTerm> Builder<S, ?> transform(Function<BT, S> function) {
            return createBasicBuilder(stream.map(e -> Maps.immutableEntry(e.getKey(), function.apply(e.getValue()))));
        }

        @Override
        public <U> Builder<BT, ?> transformOrRetain(Function<Variable, U> lookup, BiFunction<BT, U, BT> function) {
            return createBasicBuilder(stream.map(e -> Maps.immutableEntry(
                    e.getKey(),
                    Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(u -> function.apply(e.getValue(), u))
                            .orElse(e.getValue()))));
        }

        @Override
        public <U, S extends ImmutableTerm> Builder<S, ?> transformOrRemove(Function<Variable, U> lookup, Function<U, S> function) {
            return createBasicBuilder(stream
                    .flatMap(e -> Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(function)
                            .map(r -> Maps.immutableEntry(e.getKey(), r)).stream()));
        }

        @Override
        public <U> Builder<BT, ?> flatTransform(Function<Variable, U> lookup, Function<U, Substitution<BT>> function) {
            return createBasicBuilder(stream
                    .flatMap(e -> Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(function)
                            .map(Substitution::stream)
                            .orElseGet(() -> Stream.of(e))));
        }

        @Override
        public <S> Stream<S> toStream(BiFunction<Variable, BT, S> transformer) {
            return stream.map(e -> transformer.apply(e.getKey(), e.getValue()));
        }

        @Override
        public <S> ImmutableMap<Variable, S> toMap(BiFunction<Variable, BT, S> transformer) {
            return stream.collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> transformer.apply(e.getKey(), e.getValue())));
        }

        @Override
        public <S> ImmutableMap<Variable, S> toMapIgnoreOptional(BiFunction<Variable, BT, Optional<S>> transformer) {
            return stream
                    .flatMap(e -> transformer.apply(e.getKey(), e.getValue()).stream()
                            .map(v -> Maps.immutableEntry(e.getKey(), v)))
                    .collect(ImmutableCollectors.toMap());
        }
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
