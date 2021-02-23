package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * TODO: find a better name
 *
 * IMMUTABLE
 *
 * See CoreUtilsFactory for creating new instances
 *
 */
public interface VariableNullability {

    /**
     * NB: for variables outside its scope, returns true (as it does not know anything about them)
     * TODO: stop tolerating variables outside its scope and throw an exception if it happens (inconsistent with nullable groups)!
     */
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

    VariableNullability applyFreshRenaming(InjectiveVar2VarSubstitution freshRenamingSubstitution);

    /**
     * Returns a new VariableNullability.
     *
     * Treats the external variables (outside the scope) as independently nullable.
     */
    VariableNullability extendToExternalVariables(Stream<Variable> possiblyExternalVariables);

    default ImmutableSet<Variable> getNullableVariables() {
        return getNullableGroups().stream()
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());
    }
}
