package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.IntStream;

public class VariableNullabilityImpl implements VariableNullability {

    private static final VariableNullability EMPTY_SINGLETON =
            new VariableNullabilityImpl(ImmutableSet.of());

    private final ImmutableSet<ImmutableSet<Variable>> nullableGroups;

    // Lazy
    @Nullable
    private ImmutableSet<Variable> nullableVariables;
    @Nullable
    private ImmutableMap<Variable, Integer> variableMap;

    protected VariableNullabilityImpl(ImmutableSet<ImmutableSet<Variable>> nullableGroups) {
        this.nullableGroups = nullableGroups;
        this.nullableVariables = null;
    }

    protected static VariableNullability empty() {
        return EMPTY_SINGLETON;
    }

    @Override
    public boolean isPossiblyNullable(Variable variable) {
        return getNullableVariables().contains(variable);
    }

    private ImmutableSet<Variable> getNullableVariables() {
        if (nullableVariables == null)
            nullableVariables = nullableGroups.stream()
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toSet());
        return nullableVariables;
    }

    @Override
    public boolean canPossiblyBeNullSeparately(ImmutableSet<Variable> variables) {
        if (variableMap == null)
            variableMap = extractVariableMap(nullableGroups);

        return variables.stream()
                .filter(variableMap::containsKey)
                .map(variableMap::get)
                .distinct()
                .count() > 1;
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> getNullableGroups() {
        return nullableGroups;
    }

    private static ImmutableMap<Variable, Integer> extractVariableMap(
            ImmutableSet<ImmutableSet<Variable>> nullableGroups) {

        ImmutableList<ImmutableSet<Variable>> groupList = ImmutableList.copyOf(nullableGroups);
        return IntStream.range(0, groupList.size())
                .boxed()
                .flatMap(i -> groupList.get(i).stream()
                        .map(v -> Maps.immutableEntry(v, i)))
                .collect(ImmutableCollectors.toMap());
    }
}
