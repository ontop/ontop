package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class JoinOrFilterVariableNullabilityTools {

    private final TermNullabilityEvaluator nullabilityEvaluator;
    private final CoreUtilsFactory coreUtilsFactory;

    @Inject
    private JoinOrFilterVariableNullabilityTools(TermNullabilityEvaluator nullabilityEvaluator,
                                                 CoreUtilsFactory coreUtilsFactory) {
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    public VariableNullability getChildrenVariableNullability(ImmutableList<IQTree> children) {
        return getVariableNullability(children, Optional.empty());
    }

    public VariableNullability getVariableNullability(ImmutableList<IQTree> children,
                                                      Optional<ImmutableExpression> joiningCondition) {

        ImmutableMap<Variable, Collection<IQTree>> variableProvenanceMap = children.stream()
                .flatMap(c -> c.getVariables().stream()
                        .map(v -> Maps.immutableEntry(v, c)))
                .collect(ImmutableCollectors.toMultimap())
                .asMap();

        ImmutableSet<Variable> coOccuringVariables = variableProvenanceMap.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<ImmutableSet<Variable>> nullableGroups = children.stream()
                .flatMap(c -> c.getVariableNullability().getNullableGroups().stream())
                .filter(g -> g.stream()
                        .noneMatch(coOccuringVariables::contains))
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> scope = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        return joiningCondition
                .map(e -> updateWithFilter(e, nullableGroups, scope))
                .orElseGet(() -> coreUtilsFactory.createVariableNullability(nullableGroups, scope));
    }

    public VariableNullability updateWithFilter(ImmutableExpression filter,
                                                ImmutableSet<ImmutableSet<Variable>> nullableGroups,
                                                ImmutableSet<Variable> scope) {
        ImmutableSet<ImmutableSet<Variable>> newNullableGroups = nullableGroups.stream()
                .filter(g -> !nullabilityEvaluator.isFilteringNullValues(filter, g))
                .collect(ImmutableCollectors.toSet());

        return coreUtilsFactory.createVariableNullability(newNullableGroups, scope);
    }

    public VariableNullability getSimplifiedVariableNullability(ImmutableSet<Variable> variables) {
        return coreUtilsFactory.createSimplifiedVariableNullability(variables.stream());
    }
}
