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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

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

    ImmutableCollection<T> getRange();

    ImmutableSet<T> getRangeSet();

    ImmutableSet<Variable> getRangeVariables();

    T get(Variable variable);

    boolean isEmpty();

    <S extends ImmutableTerm> ImmutableSubstitution<S> transform(Function<T, S> function);

    /**
     * Applies the substitution to an immutable term.
     */
    default ImmutableTerm apply(ImmutableTerm term) {
        if (term instanceof Variable) {
            return applyToVariable((Variable) term);
        }
        if (term instanceof Constant) {
            return term;
        }
        if (term instanceof ImmutableFunctionalTerm) {
            return applyToFunctionalTerm((ImmutableFunctionalTerm) term);
        }
        throw new IllegalArgumentException("Unexpected kind of term: " + term.getClass());
    }

    /**
     * This method can be applied to simple variables
     */
    ImmutableTerm applyToVariable(Variable variable);

    ImmutableFunctionalTerm applyToFunctionalTerm(ImmutableFunctionalTerm functionalTerm);

    ImmutableExpression applyToBooleanExpression(ImmutableExpression booleanExpression);

    default ImmutableList<ImmutableTerm> applyToVariableList(ImmutableList<Variable> terms) {
        return terms.stream()
                .map(this::applyToVariable)
                .collect(ImmutableCollectors.toList());
    }


    private static VariableOrGroundTerm applyToVariableOrGroundTerm(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, VariableOrGroundTerm t) {
        if (t instanceof GroundTerm)
            return t;

        return Optional.<VariableOrGroundTerm>ofNullable(substitution.get((Variable) t)).orElse(t);
    }

    static ImmutableList<VariableOrGroundTerm> applyToVariableOrGroundTermList(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, ImmutableList<? extends VariableOrGroundTerm> terms) {
        return terms.stream()
                .map(t -> applyToVariableOrGroundTerm(substitution, t))
                .collect(ImmutableCollectors.toList());
    }

    static ImmutableMap<Integer, VariableOrGroundTerm> applyToVariableOrGroundTermArgumentMap(ImmutableSubstitution<? extends VariableOrGroundTerm> substitution, ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap) {
        return argumentMap.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> ImmutableSubstitution.applyToVariableOrGroundTerm(substitution, e.getValue())));
    }

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
