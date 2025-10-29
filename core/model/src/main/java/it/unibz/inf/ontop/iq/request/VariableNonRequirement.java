package it.unibz.inf.ontop.iq.request;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.request.impl.VariableNonRequirementImpl;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

public interface VariableNonRequirement {

    ImmutableSet<Variable> getNotRequiredVariables();

    /**
     * If {@code variable} is to be removed, then *all* the variables in the condition also need to be removed.
     * The condition does not contain the {@code variable}.
     *
     * @param variable the variable
     * @return the set of variables that need to be removed for the {@code variable} to be removed 
     */
    ImmutableSet<Variable> getCondition(Variable variable);

    VariableNonRequirement withRequiredVariables(ImmutableSet<Variable> requiredVariables);

    VariableNonRequirement withExtendedCondition(ImmutableSet<Variable> variables, ImmutableSet<Variable> extendedCondition);

    VariableNonRequirement rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory);

    /**
     * Computes the maximal subset NR of non-required variables
     * that meets (that is, contains) the condition for each element of NR.
     *
     * The non-required variables is a subset of {@code projectedVariables}
     * that does not include any of {@code requiredVariables}.
     *
     * @param projectedVariables variables projected by an {@code IQTree}
     * @param requiredVariables variables required by an {@code IQTree}
     * @return NR
     */

    ImmutableSet<Variable> computeVariablesToRemove(ImmutableSet<Variable> projectedVariables,
                                                    ImmutableSet<Variable> requiredVariables);

    boolean isEmpty();

    static VariableNonRequirement of(ImmutableSet<Variable> variables) {
        return new VariableNonRequirementImpl(variables);
    }

    static VariableNonRequirement of(ImmutableMap<Variable, ImmutableSet<Variable>> conditions) {
        return new VariableNonRequirementImpl(conditions);
    }

    static VariableNonRequirement empty() {
        return new VariableNonRequirementImpl(ImmutableSet.of());
    }
}
