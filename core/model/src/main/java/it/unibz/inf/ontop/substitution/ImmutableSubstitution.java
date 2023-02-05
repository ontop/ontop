package it.unibz.inf.ontop.substitution;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 *
 * See SubstitutionFactory for creating new instances
 *
 */
public interface ImmutableSubstitution<T extends ImmutableTerm>  {

    ImmutableSet<Map.Entry<Variable, T>> entrySet();

    boolean isDefining(Variable variable);

    ImmutableSet<Variable> getDomain();

    boolean rangeAllMatch(Predicate<T> predicate);

    boolean rangeAnyMatch(Predicate<T> predicate);

    ImmutableSet<T> getRangeSet();

    ImmutableSet<Variable> getRangeVariables();

    T get(Variable variable);

    boolean isEmpty();

    <S extends ImmutableTerm> ImmutableSubstitution<S> transform(Function<T, S> function);



    SubstitutionOperations<ImmutableTerm> onImmutableTerms();


    default ImmutableTerm apply(Variable variable) { return onImmutableTerms().apply(this, variable); }

    default ImmutableTerm applyToTerm(ImmutableTerm t) { return onImmutableTerms().applyToTerm(this, t); }

    default ImmutableFunctionalTerm apply(ImmutableFunctionalTerm term) { return onImmutableTerms().apply(this, term); }

    default ImmutableExpression apply(ImmutableExpression expression) { return onImmutableTerms().apply(this, expression); }

    default ImmutableList<ImmutableTerm> apply(ImmutableList<? extends Variable> variables) { return onImmutableTerms().apply(this, variables); }

    default ImmutableSet<ImmutableTerm> apply(ImmutableSet<? extends Variable> terms) { return onImmutableTerms().apply(this, terms); }

    default ImmutableList<ImmutableTerm> applyToTerms(ImmutableList<? extends ImmutableTerm> terms) { return onImmutableTerms().applyToTerms(this, terms); }

    default ImmutableMap<Integer, ImmutableTerm> applyToTerms(ImmutableMap<Integer, ? extends ImmutableTerm> argumentMap) { return onImmutableTerms().applyToTerms(this, argumentMap); }

    default ImmutableSubstitution<ImmutableTerm> compose(ImmutableSubstitution<? extends ImmutableTerm> f) { return onImmutableTerms().compose(this, f); }



    <S extends ImmutableTerm> ImmutableSubstitution<S> castTo(Class<S> type);

    ImmutableSubstitution<T> restrictDomainTo(Set<Variable> set);

    ImmutableSubstitution<T> removeFromDomain(Set<Variable> set);

    <S extends ImmutableTerm> ImmutableSubstitution<S> restrictRangeTo(Class<? extends S> type);

    ImmutableSubstitution<T> restrictRange(Predicate<T> predicate);

    boolean isInjective();

    ImmutableMap<T, Collection<Variable>> inverseMap();

    Builder<T> builder();

    interface Builder<T extends ImmutableTerm> {
        ImmutableSubstitution<T> build();

        <S extends ImmutableTerm> ImmutableSubstitution<S> build(Class<S> type);

        Builder<T> restrictDomainTo(Set<Variable> set);

        Builder<T> removeFromDomain(Set<Variable> set);

        Builder<T> restrict(BiPredicate<Variable, T> predicate);

        Builder<T> restrictRange(Predicate<T> predicate);

        <S extends ImmutableTerm> Builder<S> restrictRangeTo(Class<? extends S> type);

        <S extends ImmutableTerm> Builder<S> transform(BiFunction<Variable, T, S> function);

        <S extends ImmutableTerm> Builder<S> transform(Function<T, S> function);

        <U> Builder<T> transformOrRetain(Function<Variable, U> lookup, BiFunction<T, U, T> function);

        <U, S extends ImmutableTerm> Builder<S> transformOrRemove(Function<Variable, U> lookup, Function<U, S> function);

        <U> Builder<T> flatTransform(Function<Variable, U> lookup, Function<U, ImmutableSubstitution<T>> function);

        Stream<ImmutableExpression> toStrictEqualities();

        <S> Stream<S> toStream(BiFunction<Variable, T, S> transformer);

        <S> ImmutableMap<Variable, S> toMap(BiFunction<Variable, T, S> transformer);

        <S> ImmutableMap<Variable, S> toMapWithoutOptional(BiFunction<Variable, T, Optional<S>> transformer);
    }
}
