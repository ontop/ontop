package it.unibz.inf.ontop.substitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;

public interface SubstitutionOperations<T extends ImmutableTerm> extends SubstitutionComposition<T> {

    T apply(ImmutableSubstitution<? extends T> substitution, Variable variable);


    ImmutableFunctionalTerm apply(ImmutableSubstitution<? extends T> substitution, ImmutableFunctionalTerm term);

    ImmutableExpression apply(ImmutableSubstitution<? extends T> substitution, ImmutableExpression expression);

    ImmutableList<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableList<? extends Variable> variables);

    ImmutableSet<T> apply(ImmutableSubstitution<? extends T> substitution, ImmutableSet<? extends Variable> terms);

    UnifierBuilder<T> unifierBuilder();

    UnifierBuilder<T> unifierBuilder(ImmutableSubstitution<T> substitution);
}
