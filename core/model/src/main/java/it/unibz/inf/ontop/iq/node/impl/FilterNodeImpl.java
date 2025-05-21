package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.FilterNormalizer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";

    private final CoreUtilsFactory coreUtilsFactory;
    private final FilterNormalizer normalizer;

    @AssistedInject
    private FilterNodeImpl(@Assisted ImmutableExpression filterCondition, TermNullabilityEvaluator nullabilityEvaluator,
                           TermFactory termFactory, TypeFactory typeFactory, SubstitutionFactory substitutionFactory,
                           IntermediateQueryFactory iqFactory,
                           IQTreeTools iqTreeTools, ConditionSimplifier conditionSimplifier,
                           CoreUtilsFactory coreUtilsFactory, FilterNormalizer normalizer, JoinOrFilterVariableNullabilityTools variableNullabilityTools) {
        super(Optional.of(filterCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, variableNullabilityTools, conditionSimplifier, iqTreeTools);
        this.coreUtilsFactory = coreUtilsFactory;
        this.normalizer = normalizer;
    }

    @Override
    public ImmutableExpression getFilterCondition() {
        return getOptionalFilterCondition().get();
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        return variableNullabilityTools.updateWithFilter(getFilterCondition(),
                child.getVariableNullability().getNullableGroups(), child.getVariables());
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        return iqTreeTools.liftIncompatibleDefinitions(this, variable, child, variableGenerator);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator) {
        var downConstraint = new DownPropagation(Optional.of(constraint), child.getVariables());
        try {
            VariableNullability extendedChildVariableNullability = downConstraint.extendVariableNullability(child.getVariableNullability());

            // TODO: also consider the constraint for simplifying the condition
            var simplifiedFilterCondition = conditionSimplifier.simplifyCondition(
                    getFilterCondition(), ImmutableList.of(child), extendedChildVariableNullability);

            var extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(
                    downConstraint, simplifiedFilterCondition, extendedChildVariableNullability);

            IQTree newChild = extendedDownConstraint.propagate(child, variableGenerator);

            return createFilterTree(simplifiedFilterCondition, child.getVariables(), newChild);
        }
        catch (UnsatisfiableConditionException e) {
            return iqTreeTools.createEmptyNode(downConstraint);
        }

    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        checkExpression(getFilterCondition(), ImmutableList.of(child));
    }

    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        return child.getPossibleVariableDefinitions();
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.removeDistincts();
        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newChild.equals(child));
        return iqFactory.createUnaryIQTree(this, newChild, newTreeCache);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        return child.inferUniqueConstraints();
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(IQTree child, ImmutableSet<ImmutableSet<Variable>> uniqueConstraints, ImmutableSet<Variable> variables) {
        return child.inferFunctionalDependencies();
    }

    @Override
    public VariableNonRequirement computeVariableNonRequirement(IQTree child) {
        return applyFilterToVariableNonRequirement(child.getVariableNonRequirement());
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(UnaryIQTree tree, IQTree child) {
        return child.inferStrictDependents();
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return child.isConstructed(variable);
    }

    /**
     * TODO: detect minus encodings
     */
    @Override
    public boolean isDistinct(IQTree tree, IQTree child) {
        return isDistinct(tree, ImmutableList.of(child));
    }

    @Override
    public int hashCode() {
        return getFilterCondition().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FilterNodeImpl) {
            FilterNodeImpl that = (FilterNodeImpl) o;
            return getFilterCondition().equals(that.getFilterCondition());
        }
        return false;
    }

    @Override
    public String toString() {
        return FILTER_NODE_STR + getOptionalFilterString();
    }

    /**
     * TODO: Optimization: lift direct construction and filter nodes before normalizing them
     *  (so as to reduce the recursive pressure)
     */
    @Override
    public IQTree normalizeForOptimization(IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return normalizer.normalizeForOptimization(this, initialChild, variableGenerator, treeCache);
    }

    @Override
    public IQTree applyDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child, VariableGenerator variableGenerator) {

        DownPropagation downPropagation = new DownPropagation(constraint, descendingSubstitution, child.getVariables());

        ImmutableExpression unoptimizedExpression = downPropagation.applySubstitution(getFilterCondition());
        ImmutableSet<Variable> newlyProjectedVariables = downPropagation.computeProjectedVariables();

        VariableNullability simplifiedFutureChildVariableNullability = coreUtilsFactory.createSimplifiedVariableNullability(
                newlyProjectedVariables.stream());

        try {
            var simplifiedFilterCondition = conditionSimplifier.simplifyCondition(
                    unoptimizedExpression, ImmutableList.of(child), simplifiedFutureChildVariableNullability);

            DownPropagation extendedDownConstraint = conditionSimplifier.extendAndSimplifyDownConstraint(downPropagation,
                    simplifiedFilterCondition, downPropagation.extendVariableNullability(simplifiedFutureChildVariableNullability));

            IQTree newChild = extendedDownConstraint.propagate(child, variableGenerator);

            return createFilterTree(simplifiedFilterCondition, newlyProjectedVariables, newChild);
        }
        catch (UnsatisfiableConditionException e) {
            return iqTreeTools.createEmptyNode(downPropagation);
        }
    }

    private IQTree createFilterTree(ExpressionAndSubstitution simplifiedFilterCondition, ImmutableSet<Variable> projectedVariables, IQTree child) {

        var optionalFilterNode = iqTreeTools.createOptionalFilterNode(simplifiedFilterCondition.getOptionalExpression());

        var optionalConstructionNode = iqTreeTools.createOptionalConstructionNode(
                () -> projectedVariables, simplifiedFilterCondition.getSubstitution());

        return iqTreeTools.createOptionalUnaryIQTree(
                optionalConstructionNode,
                optionalFilterNode,
                child);
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child,
            VariableGenerator variableGenerator) {

        return iqFactory.createUnaryIQTree(
                iqFactory.createFilterNode(descendingSubstitution.apply(getFilterCondition())),
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator));
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);

        ImmutableExpression newCondition = renamingSubstitution.apply(getFilterCondition());

        FilterNode newFilterNode = createFilterNode(newCondition);

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(newFilterNode, newChild, newTreeCache);
    }

    private FilterNode createFilterNode(ImmutableExpression expression) {
        return expression.equals(getFilterCondition())
                ? this
                : iqFactory.createFilterNode(expression);
    }
}
