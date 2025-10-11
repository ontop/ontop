package it.unibz.inf.ontop.iq.request.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class VariableNonRequirementImpl implements VariableNonRequirement {

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
    public ImmutableSet<Variable> getNotRequiredVariables() {
        return conditions.keySet();
    }

    @Override
    public ImmutableSet<Variable> getCondition(Variable variable) {
        return conditions.getOrDefault(variable, ImmutableSet.of());
    }

    @Override
    public VariableNonRequirement withRequiredVariables(ImmutableSet<Variable> requiredVariables) {
        if (isEmpty() || requiredVariables.isEmpty())
            return this;

        return new VariableNonRequirementImpl(conditions.entrySet().stream()
                .filter(e -> !requiredVariables.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap()));
    }

    @Override
    public VariableNonRequirement withExtendedCondition(ImmutableSet<Variable> variables, ImmutableSet<Variable> extendedCondition) {
        return new VariableNonRequirementImpl(conditions.entrySet().stream()
                .map(e -> variables.contains(e.getKey())
                        ? Maps.immutableEntry(e.getKey(), Sets.difference(Sets.union(e.getValue(), extendedCondition), ImmutableSet.of(e.getKey())).immutableCopy())
                        : e)
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
        final Set<Variable> nonRequiredVariables = Sets.newHashSet(Sets.intersection(
                Sets.difference(projectedVariables, requiredVariables),
                getNotRequiredVariables()));

        while (true) {
            var variablesNotMeetingCondition = nonRequiredVariables.stream()
                    .filter(v -> !nonRequiredVariables.containsAll(getCondition(v)))
                    .collect(ImmutableCollectors.toSet());
            if (variablesNotMeetingCondition.isEmpty())
                break;
            nonRequiredVariables.removeAll(variablesNotMeetingCondition);
        }

        return ImmutableSet.copyOf(nonRequiredVariables);
    }

    @Override
    public boolean isEmpty() {
        return conditions.isEmpty();
    }
}
