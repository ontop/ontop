package it.unibz.inf.ontop.iq.request.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class VariableNonRequirementImpl implements VariableNonRequirement {

    // LAZY
    @Nullable
    private ImmutableSet<Variable> nonRequiredVariables;
    private final ImmutableMap<Variable, ImmutableSet<Variable>> conditions;


    public VariableNonRequirementImpl(ImmutableMap<Variable, ImmutableSet<Variable>> conditions) {
        this.conditions = conditions;
    }

    public VariableNonRequirementImpl(ImmutableSet<Variable> variables) {
        this(variables.stream()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        v -> ImmutableSet.of())));
    }

    @Override
    public synchronized ImmutableSet<Variable> getNotRequiredVariables() {
        if (nonRequiredVariables == null) {
            nonRequiredVariables = conditions.keySet();
        }
        return nonRequiredVariables;
    }

    @Override
    public ImmutableSet<Variable> getCondition(Variable variable) {
        return conditions.getOrDefault(variable, ImmutableSet.of());
    }

    @Override
    public VariableNonRequirement filter(BiPredicate<Variable, ImmutableSet<Variable>> predicate) {
        return new VariableNonRequirementImpl(conditions.entrySet().stream()
                .filter(e -> predicate.test(e.getKey(), e.getValue()))
                .collect(ImmutableCollectors.toMap()));
    }

    @Override
    public VariableNonRequirement rename(InjectiveSubstitution<Variable> renamingSubstitution, SubstitutionFactory substitutionFactory) {
        return new VariableNonRequirementImpl(conditions.entrySet().stream()
                .collect(ImmutableCollectors.toMap(
                        e -> substitutionFactory.apply(renamingSubstitution, e.getKey()),
                        e -> substitutionFactory.apply(renamingSubstitution, e.getValue()))));
    }

    @Override
    public ImmutableSet<Variable> computeVariablesToRemove(ImmutableSet<Variable> projectedVariables,
                                                           ImmutableSet<Variable> requiredVariables) {
        if (isEmpty())
            return ImmutableSet.of();

        // Mutable
        final Set<Variable> variablesToRemove = Sets.newHashSet(Sets.intersection(
                Sets.difference(projectedVariables, requiredVariables),
                getNotRequiredVariables()));

        while(true) {
            var variablesToKeep = variablesToRemove.stream()
                    .filter(v -> !variablesToRemove.containsAll(getCondition(v)))
                    .collect(ImmutableCollectors.toSet());
            if (variablesToKeep.isEmpty())
                break;
            variablesToRemove.removeAll(variablesToKeep);
        }

        return ImmutableSet.copyOf(variablesToRemove);
    }

    @Override
    public boolean isEmpty() {
        return conditions.isEmpty();
    }

    @Override
    public VariableNonRequirement transformConditions(BiFunction<Variable, ImmutableSet<Variable>, ImmutableSet<Variable>> fct) {
        return new VariableNonRequirementImpl(
                conditions.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> fct.apply(e.getKey(), e.getValue()))));
    }

}
