package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.FilterNormalizer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";

    private final IQTreeTools iqTreeTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final FilterNormalizer normalizer;

    @AssistedInject
    private FilterNodeImpl(@Assisted ImmutableExpression filterCondition, TermNullabilityEvaluator nullabilityEvaluator,
                           TermFactory termFactory, TypeFactory typeFactory, SubstitutionFactory substitutionFactory,
                           IntermediateQueryFactory iqFactory,
                           IQTreeTools iqTreeTools, ConditionSimplifier conditionSimplifier,
                           CoreUtilsFactory coreUtilsFactory, FilterNormalizer normalizer, JoinOrFilterVariableNullabilityTools variableNullabilityTools) {
        super(Optional.of(filterCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, variableNullabilityTools, conditionSimplifier);
        this.iqTreeTools = iqTreeTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.normalizer = normalizer;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public FilterNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableExpression getFilterCondition() {
        return getOptionalFilterCondition().get();
    }

    @Override
    public FilterNode changeFilterCondition(ImmutableExpression newFilterCondition) {
        return iqFactory.createFilterNode(newFilterCondition);
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        return variableNullabilityTools.updateWithFilter(getFilterCondition(),
                child.getVariableNullability().getNullableGroups(), child.getVariables());
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable, variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the filter
         */
        if ((newChildRoot instanceof UnionNode)
                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
            UnionNode unionNode = (UnionNode) newChildRoot;
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(unionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child, VariableGenerator variableGenerator) {
        try {
            VariableNullability extendedChildVariableNullability = child.getVariableNullability()
                    .extendToExternalVariables(constraint.getVariableStream());

            // TODO: also consider the constraint for simplifying the condition
            ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier
                    .simplifyCondition(getFilterCondition(), ImmutableList.of(child), extendedChildVariableNullability);

            Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(Optional.of(constraint),
                    conditionSimplificationResults, extendedChildVariableNullability);

            IQTree newChild = Optional.of(conditionSimplificationResults.getSubstitution())
                    .filter(s -> !s.isEmpty())
                    .map(s -> child.applyDescendingSubstitution(s, downConstraint, variableGenerator))
                    .orElseGet(() -> downConstraint
                            .map(c -> child.propagateDownConstraint(c, variableGenerator))
                            .orElse(child));

            IQTree filterLevelTree = conditionSimplificationResults.getOptionalExpression()
                    .map(e -> e.equals(getFilterCondition()) ? this : iqFactory.createFilterNode(e))
                    .<IQTree>map(filterNode -> iqFactory.createUnaryIQTree(filterNode, newChild))
                    .orElse(newChild);

            return iqTreeTools.createConstructionNodeTreeIfNontrivial(
                    filterLevelTree, conditionSimplificationResults.getSubstitution(), child::getVariables);
        }
        catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(child.getVariables());
        }

    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformFilter(tree,this, child);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree child, T context) {
        return transformer.transformFilter(tree,this, child, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitFilter(this, child);
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
        return o != null && getClass() == o.getClass()
                && getFilterCondition().equals(((FilterNode) o).getFilterCondition());
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

        ImmutableExpression unoptimizedExpression = descendingSubstitution.apply(getFilterCondition());

        ImmutableSet<Variable> newlyProjectedVariables = iqTreeTools
                .computeNewProjectedVariables(descendingSubstitution, child.getVariables());

        VariableNullability simplifiedFutureChildVariableNullability = coreUtilsFactory.createSimplifiedVariableNullability(
                newlyProjectedVariables.stream());

        try {
            ExpressionAndSubstitution expressionAndSubstitution = conditionSimplifier.simplifyCondition(
                    unoptimizedExpression, ImmutableList.of(child), simplifiedFutureChildVariableNullability);

            VariableNullability extendedVariableNullability = constraint
                    .map(c -> simplifiedFutureChildVariableNullability.extendToExternalVariables(c.getVariableStream()))
                    .orElse(simplifiedFutureChildVariableNullability);

            Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(constraint,
                    expressionAndSubstitution, extendedVariableNullability);

            Substitution<? extends VariableOrGroundTerm> downSubstitution =
                    substitutionFactory.onVariableOrGroundTerms().compose(descendingSubstitution, expressionAndSubstitution.getSubstitution());

            IQTree newChild = child.applyDescendingSubstitution(downSubstitution, downConstraint, variableGenerator);
            IQTree filterLevelTree = expressionAndSubstitution.getOptionalExpression()
                    .map(iqFactory::createFilterNode)
                    .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, newChild))
                    .orElse(newChild);

            return iqTreeTools.createConstructionNodeTreeIfNontrivial(
                    filterLevelTree, expressionAndSubstitution.getSubstitution(), () -> newlyProjectedVariables);
        }
        catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(newlyProjectedVariables);
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child,
            VariableGenerator variableGenerator) {
        FilterNode newFilterNode = iqFactory.createFilterNode(descendingSubstitution.apply(getFilterCondition()));

        return iqFactory.createUnaryIQTree(newFilterNode,
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator));
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree child, IQTreeCache treeCache) {
        IQTree newChild = child.applyFreshRenaming(renamingSubstitution);

        ImmutableExpression newCondition = renamingSubstitution.apply(getFilterCondition());

        FilterNode newFilterNode = newCondition.equals(getFilterCondition())
                ? this
                : iqFactory.createFilterNode(newCondition);

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createUnaryIQTree(newFilterNode, newChild, newTreeCache);
    }
}
