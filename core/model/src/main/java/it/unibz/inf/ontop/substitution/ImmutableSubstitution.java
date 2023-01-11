package it.unibz.inf.ontop.substitution;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.term.*;

/**
 * Declaration that the substitution is immutable and only refer to ImmutableTerms.
 *
 * See SubstitutionFactory for creating new instances
 *
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


    <S extends ImmutableTerm> ImmutableSubstitution<S> castTo(Class<S> type);

    /**
     * Constructs the projection of the substitution: the domain is restricted to the variables satisfying the filter.
     *
     * @param filter condition on variables
     * @return new restricted substitution
     */

    ImmutableSubstitution<T> filter(Predicate<Variable> filter);


    Builder<T> builder();

    interface Builder<T extends ImmutableTerm> {
        ImmutableSubstitution<T> build();

        <S extends ImmutableTerm> ImmutableSubstitution<S> build(Class<S> type);

        Builder<T> restrictDomain(Predicate<Variable> predicate);

        Builder<T> restrictDomain(ImmutableSet<Variable> set);

        Builder<T> restrict(BiPredicate<Variable, T> predicate);

        <S extends ImmutableTerm> Builder<S> restrictRangeTo(Class<S> type);

        <S extends ImmutableTerm> Builder<S> transform(BiFunction<Variable, T, S> function);

        <S extends ImmutableTerm> Builder<S> transform(Function<T, S> function);

        <U> Builder<T> conditionalTransform(Function<Variable, Optional<U>> lookup, BiFunction<T, U, T> function);

        Stream<ImmutableExpression> toStrictEqualities();

        <S> ImmutableMap<Variable, S> toMap(Function<T, S> transformer);
    }
}
