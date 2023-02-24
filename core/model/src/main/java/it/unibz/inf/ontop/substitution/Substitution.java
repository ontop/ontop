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
 * A substitution is a map from variables to a subclass of ImmutableTerm.
 * The map cannot contain entries of the form v -> v, as the apply operation
 * handles this case apply(v) = v, for any v not in the substitution domain.
 * <p>
 * The domain of a substitution consists of variables, and its range of terms.
 */
public interface Substitution<T extends ImmutableTerm>  {

    /**
     * Returns the stream of substitution entries.
     *
     * @return the stream of entries
     */
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

    /**
     * Creates a new substitution obtained by applying a given function to the
     * substitution range.
     *
     * @param function the function that transforms the terms in the range
     * @return a new substitution
     * @param <S> the type of terms of the new substitution
     */
    <S extends ImmutableTerm> Substitution<S> transform(Function<T, S> function);

    /**
     * Checks whether the substitution is injective, that is, whether no two variables
     * are mapped to the same term.
     * @return true if the substitution is injective, and false ohterwise
     */
    boolean isInjective();

    /**
     * Creates an injective substitution that consists of the same entries as the substitution.
     *
     * @return an injective substitution
     * @throws IllegalArgumentException if the substitution is not injective
     */
    InjectiveSubstitution<T> injective();


    /**
     * Returns the operations object that contains methods for dealing with
     * substitutions of the ImmutableTerm range.
     * 
     * @return operations object
     */

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



    Substitution<T> restrictDomainTo(Set<Variable> set);

    Substitution<T> removeFromDomain(Set<Variable> set);

    <S extends ImmutableTerm> Substitution<S> restrictRangeTo(Class<? extends S> type);


    ImmutableMap<T, Collection<Variable>> inverseMap();

    Builder<T, ? extends Builder<T, ?>> builder();

    interface Builder<T extends ImmutableTerm, B extends Builder<T, ? extends B>> {
        Substitution<T> build();

        B restrictDomainTo(Set<Variable> set);

        B removeFromDomain(Set<Variable> set);

        B restrict(BiPredicate<Variable, T> predicate);

        B restrictRange(Predicate<T> predicate);

        <S extends ImmutableTerm> Builder<S, ?> restrictRangeTo(Class<? extends S> type);

        <U, S extends ImmutableTerm> Builder<S, ?> transform(Function<Variable, U> lookup, BiFunction<T, U, S> function);

        <S extends ImmutableTerm> Builder<S, ?> transform(Function<T, S> function);

        <U> Builder<T, ?> transformOrRetain(Function<Variable, U> lookup, BiFunction<T, U, T> function);

        <U, S extends ImmutableTerm> Builder<S, ?> transformOrRemove(Function<Variable, U> lookup, Function<U, S> function);

        <U> Builder<T, ?> flatTransform(Function<Variable, U> lookup, Function<U, Substitution<T>> function);

        <S> Stream<S> toStream(BiFunction<Variable, T, S> transformer);

        <S> ImmutableMap<Variable, S> toMap(BiFunction<Variable, T, S> transformer);

        <S> ImmutableMap<Variable, S> toMapIgnoreOptional(BiFunction<Variable, T, Optional<S>> transformer);
    }
}
