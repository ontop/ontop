package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
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
        IQTree newChild = child.liftIncompatibleDefinitions(variable, variableGenerator);
        NaryIQTreeTools.UnionDecomposition union = NaryIQTreeTools.UnionDecomposition.of(newChild)
                      .filter(d -> d.getNode().hasAChildWithLiftableDefinition(variable, d.getChildren()));
        if (union.isPresent()) {
            return iqFactory.createNaryIQTree(
                    union.getNode(),
                    NaryIQTreeTools.transformChildren(union.getChildren(),
                        c -> iqFactory.createUnaryIQTree(this, c)));
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }


    @Override
    public IQTree applyDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child, VariableGenerator variableGenerator) {

        DownPropagation downPropagation = new DownPropagation(constraint, descendingSubstitution, child.getVariables());
        VariableNullability simplifiedFutureChildVariableNullability = coreUtilsFactory.createSimplifiedVariableNullability(
                downPropagation.computeProjectedVariables().stream());

        return propagateDownConstraint(downPropagation, child, simplifiedFutureChildVariableNullability, variableGenerator);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator) {
        var downConstraint = new DownPropagation(Optional.of(constraint), child.getVariables());
        VariableNullability extendedChildVariableNullability = downConstraint.extendVariableNullability(child.getVariableNullability());

        return propagateDownConstraint(downConstraint, child, extendedChildVariableNullability, variableGenerator);
    }

    private IQTree propagateDownConstraint(DownPropagation downConstraint, IQTree child, VariableNullability extendedChildVariableNullability, VariableGenerator variableGenerator) {
        try {
            var simplification = conditionSimplifier.simplifyAndPropagate(downConstraint,
                    Optional.of(getFilterCondition()), ImmutableList.of(child), extendedChildVariableNullability, variableGenerator);

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(simplification.getConstructionNode())
                    .append(iqTreeTools.createOptionalFilterNode(simplification.getOptionalExpression()))
                    .build(simplification.getChildren().get(0));
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
        return child.getVariableNonRequirement().withRequiredVariables(getLocallyRequiredVariables());
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

    @Override
    public IQTree normalizeForOptimization(IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        return normalizer.normalizeForOptimization(this, initialChild, variableGenerator, treeCache);
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
    public FilterNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution) {
        ImmutableExpression newCondition = renamingSubstitution.apply(getFilterCondition());
        return iqFactory.createFilterNode(newCondition);
    }

}
