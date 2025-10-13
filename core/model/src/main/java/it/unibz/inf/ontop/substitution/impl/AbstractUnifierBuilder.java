package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractUnifierBuilder<T extends ImmutableTerm> implements UnifierBuilder<T> {

    private final TermFactory termFactory;
    private final SubstitutionBasicOperations<T> operations;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Substitution<T>> optionalSubstitution;

    AbstractUnifierBuilder(TermFactory termFactory, SubstitutionBasicOperations<T> operations) {
        this.termFactory = termFactory;
        this.operations = operations;
        this.optionalSubstitution = Optional.of(termFactory.getSubstitution(ImmutableMap.of()));
    }

    @Override
    public UnifierBuilder<T> unify(Substitution<T> substitution) {
        if (optionalSubstitution.isPresent() &&  optionalSubstitution.get().isEmpty()) {
            optionalSubstitution = Optional.of(substitution);
            return this;
        }
        return unifySubstitution(substitution);
    }

    protected abstract UnifierBuilder<T> unifySubstitution(Substitution<T> substitution);

    @Override
    public UnifierBuilder<T> unify(ImmutableList<? extends T> args1, ImmutableList<? extends T> args2) {
        if (args1.size() == args2.size())
            return unify(IntStream.range(0, args1.size()), args1::get, args2::get);

        return empty();
    }

    @Override
    public UnifierBuilder<T> unify(IntStream indexes, IntFunction<? extends T> args1, IntFunction<? extends T> args2) {
        return indexes.collect(
                () -> this,
                (s, i) -> s.unify(args1.apply(i), args2.apply(i)),
                AbstractUnifierBuilder<T>::merge);
    }

    @Override
    public <B> UnifierBuilder<T> unify(Stream<B> stream, Function<B, T> args1, Function<B, T> args2) {
        return stream.collect(
                () -> this,
                (s, i) -> s.unify(args1.apply(i), args2.apply(i)),
                AbstractUnifierBuilder<T>::merge);
    }

    @Override
    public UnifierBuilder<T> unify(T t1, T t2) {
        if (optionalSubstitution.isEmpty())
            return this;

        T term1 = operations.applyToTerm(optionalSubstitution.get(), t1);
        T term2 = operations.applyToTerm(optionalSubstitution.get(), t2);

        if (term1.equals(term2))
            return this;

        return unifyUnequalTerms(term1, term2);
    }

    abstract protected UnifierBuilder<T> unifyUnequalTerms(T t1, T t2);

    /**
     * Checks that the variable is not contained in the term.
     * This method is required for avoiding unifying x with f(g(x))
     * and should be overridden for non-ground functional terms.
     *
     * @param variable variable
     * @param term that is checked to contain the variable
     * @return
     */
    protected boolean doesNotContainVariable(Variable variable, T term) {
        return true;
    }

    protected Optional<UnifierBuilder<T>> attemptUnifying(T term1, T term2) {
        if (term1 instanceof Variable) {
            Variable variable = (Variable)term1;
            if (doesNotContainVariable(variable, term2)) {
                Substitution<T> s = termFactory.getSubstitution(ImmutableMap.of(variable, term2));
                optionalSubstitution = Optional.of(operations.compose(s, optionalSubstitution.get()));
                return Optional.of(this);
            }
        }
        return Optional.empty();
    }

    protected UnifierBuilder<T> empty() {
        optionalSubstitution = Optional.empty();
        return this;
    }

    public Optional<Substitution<T>> build() {
        return optionalSubstitution;
    }

    public Optional<Substitution<T>> buildNormalized(ImmutableSet<Variable> priorityVariables) {
        return optionalSubstitution
                .map(eta -> getNormalizedUnifier(eta, priorityVariables));
    }

    private Substitution<T> getNormalizedUnifier(Substitution<T> eta, ImmutableSet<Variable> priorityVariables) {
        Substitution<T> restriction = eta.restrictDomainTo(priorityVariables);
        Substitution<T> renaming = (Substitution<T>)termFactory.getSubstitution(extractSubstitutionMap(restriction.stream(), priorityVariables));
        return operations.compose(renaming, eta);
    }

    public static <T extends ImmutableTerm> ImmutableMap<Variable, T> extractSubstitutionMap(Stream<? extends Map.Entry<T, ? extends ImmutableTerm>> stream, ImmutableSet<Variable> priorityVariables) {
        return stream
                .filter(e -> e.getValue() instanceof Variable)
                .filter(e -> !priorityVariables.contains(e.getValue()))
                .collect(ImmutableCollectors.toMap(
                        e -> (Variable)e.getValue(),
                        Map.Entry::getKey,
                        (ex, up) -> ex)); // prefer the existing value to an update
    }


    @SuppressWarnings("UnusedReturnValue")
    AbstractUnifierBuilder<T> merge(UnifierBuilder<T> another) {
        throw new MinorOntopInternalBugException("Not expected to be run in parallel");
    }
}
