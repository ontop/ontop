package it.unibz.inf.ontop.substitution;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.ConversionException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

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

    ImmutableSubstitution<T> restrictDomainTo(ImmutableSet<Variable> set);

    <S extends ImmutableTerm> ImmutableSubstitution<S> restrictRangeTo(Class<? extends S> type);

    default ImmutableSet<Variable> getRangeVariables() {
        return getRange().stream()
                .flatMap(ImmutableTerm::getVariableStream).collect(ImmutableCollectors.toSet());
    }

    default boolean isInjective() {
        ImmutableCollection<T> values = getRange();
        return values.size() == ImmutableSet.copyOf(values).size();
    }

    Builder<T> builder();

    interface Builder<T extends ImmutableTerm> {
        ImmutableSubstitution<T> build();

        <S extends ImmutableTerm> ImmutableSubstitution<S> build(Class<S> type);

        Builder<T> restrictDomainTo(ImmutableSet<Variable> set);

        Builder<T> removeFromDomain(ImmutableSet<Variable> set);

        Builder<T> restrict(BiPredicate<Variable, T> predicate);

        <S extends ImmutableTerm> Builder<S> restrictRangeTo(Class<? extends S> type);

        <S extends ImmutableTerm> Builder<S> transform(BiFunction<Variable, T, S> function);

        <S extends ImmutableTerm> Builder<S> transform(Function<T, S> function);

        <U> Builder<T> transformOrRetain(Function<Variable, U> lookup, BiFunction<T, U, T> function);

        <U, S extends ImmutableTerm> Builder<S> transformOrRemove(Function<Variable, U> lookup, Function<U, S> function);

        <U> Builder<T> flatTransform(Function<Variable, U> lookup, Function<U, Optional<ImmutableMap<Variable, T>>> function);

        Stream<ImmutableExpression> toStrictEqualities();

        <S> ImmutableMap<Variable, S> toMap(Function<T, S> transformer);

        <S> ImmutableMap<Variable, S> toMap(BiFunction<Variable, T, S> transformer);

        <S> ImmutableMap<Variable, S> toMapWithoutOptional(Function<T, Optional<S>> transformer);
    }
}
