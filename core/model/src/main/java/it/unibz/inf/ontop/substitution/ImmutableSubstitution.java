package it.unibz.inf.ontop.substitution;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.term.*;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 *
 * See SubstitutionFactory for creating new instances
 *
 * NB: implementations depend of the AtomFactory
 */
public interface ImmutableSubstitution<T extends ImmutableTerm> extends ProtoSubstitution<T> {

    /**
     * Only guaranteed for T extends VariableOrGroundTerm.
     * <p>
     * If T == ImmutableTerm, throws a ConversionException if
     * a substituted term is not a VariableOrGroundTerm.
     */
    ImmutableList<? extends VariableOrGroundTerm> applyToArguments(ImmutableList<? extends VariableOrGroundTerm> arguments) throws ConversionException;

    /**
     * Only guaranteed for T extends VariableOrGroundTerm.
     * <p>
     * If T == ImmutableTerm, throws a ConversionException if
     * a substituted term is not a VariableOrGroundTerm.
     */
    ImmutableMap<Integer, ? extends VariableOrGroundTerm> applyToArgumentMap(ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap)
            throws ConversionException;

    /**
     * Viewing a substitution as a function (takes a term, returns a term).
     * This method yield the substitution "(g o f)" where g is this substitution.
     * NB: (g o f)(x) = g(f(x))
     */
    ImmutableSubstitution<ImmutableTerm> composeWith(ImmutableSubstitution<? extends ImmutableTerm> f);

    ImmutableSubstitution<T> composeWith2(ImmutableSubstitution<? extends T> f);

    /**
     * Because of the optional cannot be overloaded.
     */
    Optional<ImmutableSubstitution<T>> union(ImmutableSubstitution<T> otherSubstitution);

    /**
     * Returns a "similar" substitution that avoids (if possible) to substitute certain variables.
     * <p>
     * Acts on equality between variables.
     * <p>
     * The first variable in the list has the highest priority.
     * <p>
     * This method requires the domain and the range to be disjoint.
     */
    ImmutableSubstitution<T> orientate(ImmutableList<Variable> priorityVariables);

    <S extends ImmutableTerm> ImmutableSubstitution<S> getFragment(Class<S> type);

    /**
     * Constructs the projection of the substitution: the domain is restricted to the variables satisfying the filter.
     *
     * @param filter condition on variables
     * @return new restricted substitution
     */

    ImmutableSubstitution<T> filter(Predicate<Variable> filter);

    /**
     * Constructs the projection of the substitution: the domain is restricted to the variables satisfying the filter.
     *
     * @param filter condition on variables
     * @return new restricted substitution
     */

    ImmutableSubstitution<T> filter(BiPredicate<Variable, T> filter);

    /**
     * Constructs a new substitution by applying function to the range.
     *
     * @param function value transformation function
     * @param <S> the type of the resulting terms
     * @return new transformed substitution
     */
    <S extends ImmutableTerm> ImmutableSubstitution<S> transform(Function<T, S> function);

    /**
     * Constructs a new substitution by applying function to the range.
     *
     * @param function value transformation function
     * @param <S> the type of the resulting terms
     * @return new transformed substitution
     */

    <S extends ImmutableTerm> ImmutableSubstitution<S> transform(BiFunction<Variable, T, S> function);
}
