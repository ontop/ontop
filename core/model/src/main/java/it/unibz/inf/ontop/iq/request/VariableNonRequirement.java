package it.unibz.inf.ontop.iq.request;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.request.impl.VariableNonRequirementImpl;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public interface VariableNonRequirement {

    ImmutableSet<Variable> getNotRequiredVariables();

    /**
     * Variables that must be removed for being able to remove the variable passed as parameter
     */
    ImmutableSet<Variable> getCondition(Variable variable);

    VariableNonRequirement filter(BiPredicate<Variable, ImmutableSet<Variable>> predicate);

    VariableNonRequirement rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory);

    ImmutableSet<Variable> computeVariablesToRemove(ImmutableSet<Variable> projectedVariables,
                                                    ImmutableSet<Variable> requiredVariables);

    boolean isEmpty();

    VariableNonRequirement transformConditions(BiFunction<Variable, ImmutableSet<Variable>, ImmutableSet<Variable>> fct);

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
