package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * TODO: find a better name
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

}
