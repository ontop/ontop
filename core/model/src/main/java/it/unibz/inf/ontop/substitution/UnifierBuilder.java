package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Creates a unifier for args1 and args2
 *
 * The operation is as follows
 *
 * {x/y, m/y} composed with (y,z) is equal to {x/z, m/z, y/z}
 *
 */

public interface UnifierBuilder<T extends ImmutableTerm> {

    UnifierBuilder<T> unify(ImmutableList<? extends T> args1, ImmutableList<? extends T> args2);

    UnifierBuilder<T> unify(IntStream indexes, IntFunction<? extends T> args1, IntFunction<? extends T> args2);

    <B> UnifierBuilder<T> unify(Stream<B> stream, Function<B, T> args1, Function<B, T> args2);

    UnifierBuilder<T> unify(T t1, T t2);

    UnifierBuilder<T> unify(Substitution<T> substitution);

    Optional<Substitution<T>> build();

    /**
     * Normalizes eta so as to avoid projected variables to be substituted by non-projected variables.
     *
     * This normalization can be understood as a way to select a MGU (eta) among a set of equivalent MGUs.
     * Such a "selection" is done a posteriori.
     */

    Optional<Substitution<T>> buildNormalized(ImmutableSet<Variable> priorityVariables);
}