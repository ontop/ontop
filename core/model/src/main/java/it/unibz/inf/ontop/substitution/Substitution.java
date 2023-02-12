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
public interface Substitution<T extends ImmutableTerm>  {

    Stream<Map.Entry<Variable, T>> stream();

    /**
     * Returns true if the variable is in the domain of the substitution.
     *
     * @param variable a variable
     * @return true if the variable is in the domain, and false otherwise
     */
    boolean isDefining(Variable variable);

    /**
     * Returns the domain of the substitution.
     * The domain consists of all variables on which applying the substitution
     * gives a non-identity result.
     *
     * @return the domain
     */
    ImmutableSet<Variable> getDomain();

    /**
     * Returns the subset of the domain that is mapped to terms satisfying the predicate,
     * which is the pre-image of the set of terms satisfying the predicate.
     *
     * @param predicate a predicate
     * @return the set of variables that are mapped to terms satisfying the predicate
     */
    ImmutableSet<Variable> getPreImage(Predicate<T> predicate);

    /**
     * Returns the set of terms that forms range of the substitution.
     *
     * @return the range of the substitution
     */
    ImmutableSet<T> getRangeSet();

    /**
     * Returns the set of variables occurring in the range of the substitution.
     *
     * @return the set of variables in the range
     */
    ImmutableSet<Variable> getRangeVariables();

    /**
     * Checks whether all terms in the range satisfy a given predicate.
     *
     * @param predicate a predicate
     * @return true if all terms in the range satisfy the predicate, and false otherwise
     */
    boolean rangeAllMatch(Predicate<T> predicate);

    /**
     * Checks whether at least one term in the range satisfies the given predicate.
     *
     * @param predicate a predicate
     * @return true if at least one term in the range satisfies the predicate, and false otherwise
     */
    boolean rangeAnyMatch(Predicate<T> predicate);

    /**
     * Returns the term associated by the substitution with a given variable,
     * or null if the variable is not in the domain.
     *
     * @param variable a variable
     * @return the term associated with the variable,
     *          or null if the variable is not in the domain
     */
    T get(Variable variable);

    /**
     * Returns true if the substitution is empty (when its domain is empty).
     *
     * @return true if the substitution is empty, and false otherwise
     */
    boolean isEmpty();

    <S extends ImmutableTerm> Substitution<S> transform(Function<T, S> function);


    boolean isInjective();

    InjectiveSubstitution<T> injective();

    SubstitutionOperations<ImmutableTerm> onImmutableTerms();


    default ImmutableTerm apply(Variable variable) { return onImmutableTerms().apply(this, variable); }

    default ImmutableTerm applyToTerm(ImmutableTerm t) { return onImmutableTerms().applyToTerm(this, t); }

    default ImmutableFunctionalTerm apply(ImmutableFunctionalTerm term) { return onImmutableTerms().apply(this, term); }

    default ImmutableExpression apply(ImmutableExpression expression) { return onImmutableTerms().apply(this, expression); }

    default ImmutableList<ImmutableTerm> apply(ImmutableList<? extends Variable> variables) { return onImmutableTerms().apply(this, variables); }

    default ImmutableSet<ImmutableTerm> apply(ImmutableSet<? extends Variable> terms) { return onImmutableTerms().apply(this, terms); }

    default ImmutableList<ImmutableTerm> applyToTerms(ImmutableList<? extends ImmutableTerm> terms) { return onImmutableTerms().applyToTerms(this, terms); }

    default ImmutableMap<Integer, ImmutableTerm> applyToTerms(ImmutableMap<Integer, ? extends ImmutableTerm> argumentMap) { return onImmutableTerms().applyToTerms(this, argumentMap); }

    default Substitution<ImmutableTerm> compose(Substitution<? extends ImmutableTerm> f) { return onImmutableTerms().compose(this, f); }




    <S extends ImmutableTerm> Substitution<S> castTo(Class<S> type);

    Substitution<T> restrictDomainTo(Set<Variable> set);

    Substitution<T> removeFromDomain(Set<Variable> set);

    <S extends ImmutableTerm> Substitution<S> restrictRangeTo(Class<? extends S> type);


    ImmutableMap<T, Collection<Variable>> inverseMap();

    Builder<T> builder();

    interface Builder<T extends ImmutableTerm> {
        Substitution<T> build();

        <S extends ImmutableTerm> Substitution<S> build(Class<S> type);

        Builder<T> restrictDomainTo(Set<Variable> set);

        Builder<T> removeFromDomain(Set<Variable> set);

        Builder<T> restrict(BiPredicate<Variable, T> predicate);

        Builder<T> restrictRange(Predicate<T> predicate);

        <S extends ImmutableTerm> Builder<S> restrictRangeTo(Class<? extends S> type);

        <U, S extends ImmutableTerm> Builder<S> transform(Function<Variable, U> lookup, BiFunction<T, U, S> function);

        <S extends ImmutableTerm> Builder<S> transform(Function<T, S> function);

        <U> Builder<T> transformOrRetain(Function<Variable, U> lookup, BiFunction<T, U, T> function);

        <U, S extends ImmutableTerm> Builder<S> transformOrRemove(Function<Variable, U> lookup, Function<U, S> function);

        <U> Builder<T> flatTransform(Function<Variable, U> lookup, Function<U, Substitution<T>> function);

        Stream<ImmutableExpression> toStrictEqualities();

        <S> Stream<S> toStream(BiFunction<Variable, T, S> transformer);

        <S> ImmutableMap<Variable, S> toMap(BiFunction<Variable, T, S> transformer);

        <S> ImmutableMap<Variable, S> toMapWithoutOptional(BiFunction<Variable, T, Optional<S>> transformer);
    }
}
