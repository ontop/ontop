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

    public SubstitutionImpl(ImmutableMap<Variable, ? extends T> map,  TermFactory termFactory, boolean checkEntries) {
        this.termFactory = termFactory;
        this.defaultOperations = new ImmutableTermsSubstitutionOperations(termFactory);
        //noinspection unchecked
        this.map = (ImmutableMap<Variable, T>) map;

        if (checkEntries && this.map.entrySet().stream().anyMatch(e -> e.getKey().equals(e.getValue())))
            throw new IllegalArgumentException("Please do not insert entries like t/t in your substitution " +
                    "(for efficiency reasons)\n. Substitution: " + this.map);
    }

    protected  <T extends ImmutableTerm, S extends ImmutableTerm> Substitution<S> createSubstitution(Stream<Map.Entry<Variable, T>> stream, Function<Map.Entry<Variable, T>, S> mapper, boolean checkEntries) {
        return new SubstitutionImpl<>(stream.collect(ImmutableCollectors.toMap(Map.Entry::getKey, mapper)), termFactory, checkEntries);
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
       return stream()
               .collect(ImmutableCollectors.toMultimap(Map.Entry::getValue, Map.Entry::getKey))
               .asMap();
    }

    @Override
    public ImmutableSet<Variable> getPreImage(Predicate<T> predicate) {
        return stream()
                .filter(e -> predicate.test(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public Substitution<T> restrictDomainTo(Set<Variable> set) {
        return createSubstitution(stream().filter(e -> set.contains(e.getKey())), Map.Entry::getValue, false);
    }

    @Override
    public Substitution<T> removeFromDomain(Set<Variable> set) {
        return createSubstitution(stream().filter(e -> !set.contains(e.getKey())), Map.Entry::getValue, false);
    }

    @Override
    public <S extends ImmutableTerm> Substitution<S> restrictRangeTo(Class<? extends S> type) {
        return createSubstitution(stream().filter(e -> type.isInstance(e.getValue())), e -> type.cast(e.getValue()), false);
    }

    @Override
    public <S extends ImmutableTerm> Substitution<S> transform(Function<T, S> function) {
        return createSubstitution(stream(), e -> function.apply(e.getValue()), true);
    }

    @Override
    public InjectiveSubstitution<T> injective() {
        return new InjectiveSubstitutionImpl<>(map, termFactory, true);
    }

    @Override
    public Builder<T, ? extends Builder<T, ?>> builder() {
        return new BuilderImpl<>(stream(), false);
    }


    protected class BuilderImpl<BT extends ImmutableTerm> extends AbstractBuilderImpl<BT, BuilderImpl<BT>> {
        BuilderImpl(Stream<Map.Entry<Variable, BT>> stream, boolean checkEntries) {
            super(stream, checkEntries);
        }
        @Override
        protected BuilderImpl<BT> createBuilder(Stream<Map.Entry<Variable, BT>> stream, boolean checkEntries) {
            return new BuilderImpl<>(stream, checkEntries);
        }
        @Override
        public Substitution<BT> build() {
            return createSubstitution(stream, Map.Entry::getValue, checkEntries);
        }
    }

    protected abstract class AbstractBuilderImpl<BT extends ImmutableTerm, B extends Builder<BT, ? extends B>> implements Builder<BT, B> {
        protected final Stream<Map.Entry<Variable, BT>> stream;
        protected final boolean checkEntries;

        AbstractBuilderImpl(Stream<Map.Entry<Variable, BT>> stream, boolean checkEntries) {
            this.stream = stream;
            this.checkEntries = checkEntries;
        }

        private  <S extends ImmutableTerm> BuilderImpl<S> createBasicBuilder(Stream<Map.Entry<Variable, S>> stream, boolean checkEntries) {
            return new BuilderImpl<>(stream, checkEntries);
        }

        protected abstract B createBuilder(Stream<Map.Entry<Variable, BT>> stream, boolean checkEntries);

        @Override
        public B restrictDomainTo(Set<Variable> set) {
            return createBuilder(stream.filter(e -> set.contains(e.getKey())), checkEntries);
        }

        @Override
        public B removeFromDomain(Set<Variable> set) {
            return createBuilder(stream.filter(e -> !set.contains(e.getKey())), checkEntries);
        }

        @Override
        public B restrict(BiPredicate<Variable, BT> predicate) {
            return createBuilder(stream.filter(e -> predicate.test(e.getKey(), e.getValue())), checkEntries);
        }

        @Override
        public B restrictRange(Predicate<BT> predicate) {
            return createBuilder(stream.filter(e -> predicate.test(e.getValue())), checkEntries);
        }

        @Override
        public <S extends ImmutableTerm> Builder<S, ?> restrictRangeTo(Class<? extends S> type) {
            return createBasicBuilder(stream
                    .filter(e -> type.isInstance(e.getValue()))
                    .map(e -> Maps.immutableEntry(e.getKey(), type.cast(e.getValue()))), checkEntries);
        }

        @Override
        public <U, S extends ImmutableTerm> Builder<S, ?> transform(Function<Variable, U> lookup, BiFunction<BT, U, S> function) {
            return createBasicBuilder(stream.map(e -> Maps.immutableEntry(
                    e.getKey(),
                    function.apply(e.getValue(), lookup.apply(e.getKey())))), true);
        }

        @Override
        public <S extends ImmutableTerm> Builder<S, ?> transform(Function<BT, S> function) {
            return createBasicBuilder(stream.map(e -> Maps.immutableEntry(e.getKey(), function.apply(e.getValue()))), true);
        }

        @Override
        public <U> Builder<BT, ?> transformOrRetain(Function<Variable, U> lookup, BiFunction<BT, U, BT> function) {
            return createBasicBuilder(stream.map(e -> Maps.immutableEntry(
                    e.getKey(),
                    Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(u -> function.apply(e.getValue(), u))
                            .orElse(e.getValue()))), true);
        }

        @Override
        public <U, S extends ImmutableTerm> Builder<S, ?> transformOrRemove(Function<Variable, U> lookup, Function<U, S> function) {
            return createBasicBuilder(stream
                    .flatMap(e -> Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(function)
                            .map(r -> Maps.immutableEntry(e.getKey(), r)).stream()), true);
        }

        @Override
        public <U> Builder<BT, ?> flatTransform(Function<Variable, U> lookup, Function<U, Substitution<BT>> function) {
            return createBasicBuilder(stream
                    .flatMap(e -> Optional.ofNullable(lookup.apply(e.getKey()))
                            .map(function)
                            .map(Substitution::stream)
                            .orElseGet(() -> Stream.of(e))), true);
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
