package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;

import java.util.Optional;
import java.util.stream.Collector;

/**
 * Operations that are available only to Substitution classes where T has Variable as a subclass
 *
 * @param <T> the type of elements in the range of the substitution
 */

public interface SubstitutionOperations<T extends ImmutableTerm> extends SubstitutionBasicOperations<T> {

    T apply(Substitution<? extends T> substitution, Variable variable);


    ImmutableFunctionalTerm apply(Substitution<? extends T> substitution, ImmutableFunctionalTerm term);

    ImmutableExpression apply(Substitution<? extends T> substitution, ImmutableExpression expression);

    ImmutableList<T> apply(Substitution<? extends T> substitution, ImmutableList<? extends Variable> variables);

    ImmutableSet<T> apply(Substitution<? extends T> substitution, ImmutableSet<? extends Variable> terms);


    UnifierBuilder<T> unifierBuilder();

    UnifierBuilder<T> unifierBuilder(Substitution<T> substitution);

    Collector<Substitution<T>, ?, Optional<Substitution<T>>> toUnifier();

    default Optional<Substitution<T>> unify(T t1, T t2) { return unifierBuilder().unify(t1, t2).build(); }

    interface ArgumentMapUnifier<T extends ImmutableTerm> {
        ImmutableMap<Integer, ? extends T> getArgumentMap();

        Substitution<T> getSubstitution();
    }

    Collector<ImmutableMap<Integer, ? extends T>, ?, Optional<ArgumentMapUnifier<T>>> toArgumentMapUnifier();
}