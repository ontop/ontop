package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

/**
 * TODO: find a better name
 *
 * IMMUTABLE
 */
public interface VariableNullability {

    boolean isPossiblyNullable(Variable variable);

    /**
     * Returns true if it possible that among the variables at least two can be null
     * but are not required to be null at the same time.
     */
    boolean canPossiblyBeNullSeparately(ImmutableSet<Variable> variables);

    /**
     * Generalization to arbitrary terms
     */
    boolean canPossiblyBeNullSeparately(ImmutableList<? extends ImmutableTerm> terms);

    /**
     * All the variables of a group are always null at the same time (guaranteed).
     *
     */
    ImmutableSet<ImmutableSet<Variable>> getNullableGroups();

    VariableNullability update(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                               ImmutableSet<Variable> projectedVariables);
}
