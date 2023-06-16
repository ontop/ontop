package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.*;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class JoinOrFilterNodeImpl extends CompositeQueryNodeImpl implements JoinOrFilterNode {

    private final Optional<ImmutableExpression> optionalFilterCondition;

    protected final TermNullabilityEvaluator nullabilityEvaluator;
    protected final TypeFactory typeFactory;
    protected final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    protected final ConditionSimplifier conditionSimplifier;

    protected JoinOrFilterNodeImpl(Optional<ImmutableExpression> optionalFilterCondition,
                                   TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                                   IntermediateQueryFactory iqFactory, TypeFactory typeFactory,
                                   SubstitutionFactory substitutionFactory,
                                   JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier) {
        super(substitutionFactory, termFactory, iqFactory);
        this.optionalFilterCondition = optionalFilterCondition;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.typeFactory = typeFactory;
        this.variableNullabilityTools = variableNullabilityTools;
        this.conditionSimplifier = conditionSimplifier;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }

    protected String getOptionalFilterString() {
        return getOptionalFilterCondition()
                .map(f -> " " + f)
                .orElse("");
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        return getOptionalFilterCondition()
                .map(ImmutableFunctionalTerm::getVariables)
                .orElse(ImmutableSet.of());
    }

    protected boolean isFilteringNullValue(Variable variable) {
        return getOptionalFilterCondition()
                .filter(e -> nullabilityEvaluator.isFilteringNullValue(e, variable))
                .isPresent();
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    protected void checkExpression(ImmutableExpression expression, ImmutableList<IQTree> children)
            throws InvalidIntermediateQueryException {

        ImmutableSet<Variable> childrenVariables = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> unboundVariables = expression.getVariableStream()
                .filter(v -> !childrenVariables.contains(v))
                .collect(ImmutableCollectors.toSet());
        if (!unboundVariables.isEmpty()) {
            throw new InvalidIntermediateQueryException("Expression " + expression + " of "
                    + expression + " uses unbound variables (" + unboundVariables +  ").\n" + this);
        }
    }

    protected VariableNonRequirement applyFilterToVariableNonRequirement(VariableNonRequirement nonRequirementBeforeFilter,
                                                                         ImmutableList<IQTree> children) {
        return applyFilterToVariableNonRequirement(nonRequirementBeforeFilter);
    }

    protected VariableNonRequirement applyFilterToVariableNonRequirement(VariableNonRequirement nonRequirementBeforeFilter) {
        ImmutableSet<Variable> filterVariables = getLocallyRequiredVariables();

        return nonRequirementBeforeFilter
                .filter((v, conds) -> !filterVariables.contains(v));
    }

    protected boolean isDistinct(IQTree tree, ImmutableList<IQTree> children) {
        if (children.stream().noneMatch(child -> mayChildCauseParentBeingNonDistinct(tree, child)))
            return true;


        ImmutableSet<ImmutableSet<Variable>> constraints = tree.inferUniqueConstraints();
        if (constraints.isEmpty())
            return false;

        VariableNullability variableNullability = tree.getVariableNullability();
        return constraints.stream()
                .anyMatch(c -> c.stream().noneMatch(variableNullability::isPossiblyNullable));
    }

    /**
     * Must not return false negative
     */
    private static boolean mayChildCauseParentBeingNonDistinct(IQTree parentTree, IQTree child) {
        if (child.isDistinct())
            return false;

        // Useful when the unique constraint from the child is "nullable" at the child level
        // but not at the parent level (thanks to implicit and explicit conditions)
        VariableNullability parentVariableNullability = parentTree.getVariableNullability();
        return child.inferUniqueConstraints().stream()
                .noneMatch(c -> c.stream().noneMatch(parentVariableNullability::isPossiblyNullable));
    }
}
