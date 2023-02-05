package it.unibz.inf.ontop.substitution.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionOperations;
import it.unibz.inf.ontop.substitution.UnifierBuilder;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractUnifierBuilder<T extends ImmutableTerm> implements UnifierBuilder<T> {

    private final TermFactory termFactory;
    private final SubstitutionOperations<T> operations;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<ImmutableSubstitution<T>> optionalSubstitution;

    AbstractUnifierBuilder(TermFactory termFactory, SubstitutionOperations<T> operations, ImmutableSubstitution<T> substitution) {
        this.termFactory = termFactory;
        this.operations = operations;
        this.optionalSubstitution = Optional.of(substitution);
    }

    @Override
    public UnifierBuilder<T> unifyTermLists(ImmutableList<? extends T> args1, ImmutableList<? extends T> args2) {
        if (args1.size() == args2.size())
            return unifyTermStreams(IntStream.range(0, args1.size()), args1::get, args2::get);

        return emptySelf();
    }

    @Override
    public UnifierBuilder<T> unifyTermStreams(IntStream indexes, IntFunction<? extends T> args1, IntFunction<? extends T> args2) {
        return indexes.collect(
                () -> this,
                (s, i) -> s.unifyTerms(args1.apply(i), args2.apply(i)),
                AbstractUnifierBuilder<T>::merge);
    }

    @Override
    public <B> UnifierBuilder<T> unifyTermStreams(Stream<B> stream, Function<B, T> args1, Function<B, T> args2) {
        return stream.collect(
                () -> this,
                (s, i) -> s.unifyTerms(args1.apply(i), args2.apply(i)),
                AbstractUnifierBuilder<T>::merge);
    }

    @Override
    public UnifierBuilder<T> unifyTerms(T t1, T t2) {
        if (optionalSubstitution.isEmpty())
            return this;

        T term1 = operations.applyToTerm(optionalSubstitution.get(), t1);
        T term2 = operations.applyToTerm(optionalSubstitution.get(), t2);

        if (term1.equals(term2))
            return this;

        return unifyUnequalTerms(term1, term2);
    }

    abstract protected UnifierBuilder<T> unifyUnequalTerms(T t1, T t2);

    protected UnifierBuilder<T> extendSubstitution(Variable variable, T term) {
        ImmutableSubstitution<T> s = termFactory.getSubstitution(ImmutableMap.of(variable, term));
        optionalSubstitution = Optional.of(operations.compose(s, optionalSubstitution.get()));
        return this;
    }

    protected UnifierBuilder<T> emptySelf() {
        optionalSubstitution = Optional.empty();
        return this;
    }

    public Optional<ImmutableSubstitution<T>> build() {
        return optionalSubstitution;
    }

    @SuppressWarnings("UnusedReturnValue")
    private UnifierBuilder<T> merge(UnifierBuilder<T> another) {
        throw new MinorOntopInternalBugException("Not expected to be run in parallel");
    }
}
