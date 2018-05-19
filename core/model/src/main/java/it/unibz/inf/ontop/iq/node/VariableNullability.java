package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;

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
     * All the variables of a group are always null at the same time (guaranteed).
     *
     */
    ImmutableSet<ImmutableSet<Variable>> getNullableGroups();

    /**
     * Creates a new (immutable) VariableNullability
     *
     * For each entry (k,v) where k is a novel variable,
     *  - if (k == v): create a new nullable group for k
     *
     *  - else k is bound to v and therefore is added to the nullable group of v.
     *
     *  Invalid input entry:
     *    - k is already in a nullable group
     *    - v != k is not already in a nullable group
     *
     */
    VariableNullability appendNewVariables(ImmutableMap<Variable, Variable> nullabilityBindings);
}
