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


    /**
     * Restricts the domain to a given set of variables.
     *
     * @param set a set of variables
     * @return new substitution
     */

    Substitution<T> restrictDomainTo(Set<Variable> set);

    /**
     * Removes a given set of variables from the domain.
     *
     * @param set a set of variables
     * @return new substitution
     */

    Substitution<T> removeFromDomain(Set<Variable> set);

    /**
     * Restricts the range to the terms of a given type.
     *
     * @param <S> possibly a supertype of the new range type
     * @param type a type literal
     * @return new substitution
     */

    <S extends ImmutableTerm> Substitution<S> restrictRangeTo(Class<? extends S> type);


    ImmutableMap<T, Collection<Variable>> inverseMap();

    /**
     * Returns a builder object to construct a new substitution on the basis of this.
     *
     * @return a builder
     */
    Builder<T, ? extends Builder<T, ?>> builder();

    /**
     * Represents a series of transformation of a substitution into another substitution (or stream/map).
     *
     * @param <T> the range type
     * @param <B> the builder type (to handle injectivity)
     */

    interface Builder<T extends ImmutableTerm, B extends Builder<T, ? extends B>> {
        /**
         * Completes the transformation pipeline and constructs a substitution.
         *
         * @return the substitution
         */
        Substitution<T> build();

        /**
         * Restricts the domain to a given set of variables.
         *
         * @param set a set of variables
         * @return the builder object
         */
        B restrictDomainTo(Set<Variable> set);

        /**
         * Removes a given set of variables from the domain.
         *
         * @param set a set of variables
         * @return the builder object
         */
        B removeFromDomain(Set<Variable> set);


        /**
         * Restricts the domain and range based on a binary predicate.
         *
         * @param predicate a predicate
         * @return the builder object
         */
        B restrict(BiPredicate<Variable, T> predicate);

        /**
         * Restricts the range to the terms satisfying a given predicate.
         *
         * @param predicate a predicate
         * @return the builder object
         */
        B restrictRange(Predicate<T> predicate);

        /**
         * Restricts the range to the terms of a given type.
         *
         * @param <S> possibly a supertype of the new range type
         * @param type a type literal
         * @return the builder object
         */
        <S extends ImmutableTerm> Builder<S, ?> restrictRangeTo(Class<? extends S> type);

        /**
         * Transforms the terms in the range according a given function
         *
         * @param function maps terms in the range to new range terms
         * @return the builder object
         * @param <S> type of the transformed range terms
         */
        <S extends ImmutableTerm> Builder<S, ?> transform(Function<T, S> function);

        /**
         * Transforms the terms in the range according a given total look-up and function.
         * The look-up function is assumed to be defined on the whole domain.
         *
         * @param lookup maps domain variables to values of the look-up type
         * @param function maps terms in the range and look-up values to new range terms
         * @return the builder object
         * @param <U> type of the look-up objects
         * @param <S> type of the transformed range terms
         */
        <U, S extends ImmutableTerm> Builder<S, ?> transform(Function<Variable, U> lookup, BiFunction<T, U, S> function);

        /**
         * Transforms the terms in the range according a given partial look-up and function.
         * If the look-up function is undefined on a domain element, the old value
         * is preserved.
         *
         * @param lookup maps domain variables to values of the look-up type
         * @param function maps terms in the range and look-up values to new range terms
         * @return the builder object
         * @param <U> type of the look-up objects
         */
        <U> Builder<T, ?> transformOrRetain(Function<Variable, U> lookup, BiFunction<T, U, T> function);

        /**
         * Transforms the terms in the range according a given partial look-up and function.
         * If the look-up function is undefined on a domain element, the domain element and
         * old its associated term are removed.
         *
         * @param lookup maps domain variables to values of the look-up type
         * @param function maps look-up values to new range terms
         * @return the builder object
         * @param <U> type of the look-up objects
         * @param <S> type of the transformed range terms
         */
        <U, S extends ImmutableTerm> Builder<S, ?> transformOrRemove(Function<Variable, U> lookup, Function<U, S> function);

        /**
         * Transforms the terms in the range according a given partial look-up and function.
         * If the look-up function is undefined on a domain element, the old value
         * is preserved. If the look-up function is defined, then the single entry is replaced
         * by a set of entries determined by the function.
         *
         * @param lookup maps domain variables to values of the look-up type
         * @param function maps look-up values to replacing sets of entries (as a substitution)
         * @return the builder object
         * @param <U> type of the look-up objects
         */
        <U> Builder<T, ?> flatTransform(Function<Variable, U> lookup, Function<U, Substitution<T>> function);

        /**
         * Completes the transformation pipeline and returns a stream
         *
         * @param transformer a function that maps variables and terms to stream elements
         * @param <S> the type of stream elements
         * @return the stream
         */

        <S> Stream<S> toStream(BiFunction<Variable, T, S> transformer);

        /**
         * Completes the transformation pipeline and returns a map from variables to some values
         *
         * @param transformer a function that maps variables and terms to map values
         * @param <S> the type of map values
         * @return the map
         */

        <S> ImmutableMap<Variable, S> toMap(BiFunction<Variable, T, S> transformer);

        /**
         * Completes the transformation pipeline and returns a map from variables to some values.
         * Optional map values and the corresponding variables are removed from the map
         *
         * @param transformer a function that maps variables and terms to optional map values
         * @param <S> the type of map values
         * @return the map
         */

        <S> ImmutableMap<Variable, S> toMapIgnoreOptional(BiFunction<Variable, T, Optional<S>> transformer);
    }
}
