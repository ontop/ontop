package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;

import java.util.Optional;
import java.util.stream.Collector;

public interface SubstitutionOperations<T extends ImmutableTerm> extends SubstitutionComposition<T> {

    T apply(ImmutableSubstitution<? extends T> substitution, Variable variable);


    ImmutableFunctionalTerm apply(ImmutableSubstitution<? extends T> substitution, ImmutableFunctionalTerm term);

    ImmutableExpression apply(ImmutableSubstitution<? extends T> substitution, ImmutableExpression expression);

    ImmutableList<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends Variable> variables);

    ImmutableSet<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableSet<? extends Variable> terms);


    UnifierBuilder<T> unifierBuilder();

    UnifierBuilder<T> unifierBuilder(ImmutableSubstitution<T> substitution);

    Collector<ImmutableSubstitution<T>, ?, Optional<ImmutableSubstitution<T>>> toUnifier();

    default Optional<ImmutableSubstitution<T>> unify(T t1, T t2) { return unifierBuilder().unify(t1, t2).build(); }

    interface ArgumentMapUnifier<T extends ImmutableTerm> {
        ImmutableMap<Integer, ? extends T> getArgumentMap();

        ImmutableSubstitution<T> getSubstitution();
    }

    Collector<ImmutableMap<Integer, ? extends T>, ?, Optional<ArgumentMapUnifier<T>>> toArgumentMapUnifier();
}