package it.unibz.inf.ontop.iq.request;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.request.impl.VariableNonRequirementImpl;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public interface VariableNonRequirement {

    ImmutableSet<Variable> getNotRequiredVariables();

    /**
     * Variables that must be removed for being able to remove the variable passed as parameter
     */
    ImmutableSet<Variable> getCondition(Variable variable);

    Stream<Map.Entry<Variable, ImmutableSet<Variable>>> entryStream();

    VariableNonRequirement filter(BiPredicate<Variable, ImmutableSet<Variable>> predicate);

    VariableNonRequirement rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory);

    ImmutableSet<Variable> computeVariablesToRemove(ImmutableSet<Variable> projectedVariables,
                                                    ImmutableSet<Variable> requiredVariables);

    static VariableNonRequirement of(ImmutableSet<Variable> variables) {
        return new VariableNonRequirementImpl(variables);
    }

    static VariableNonRequirement empty() {
        return new VariableNonRequirementImpl(ImmutableSet.of());
    }

    boolean isEmpty();
}
