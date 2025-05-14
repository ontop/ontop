package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.LeftJoinNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.impl.ExpressionAndSubstitutionImpl;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.iq.request.VariableNonRequirement;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.*;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class LeftJoinNodeImpl extends JoinLikeNodeImpl implements LeftJoinNode {

    private static final String LEFT_JOIN_NODE_STR = "LJ";
    private final LeftJoinNormalizer ljNormalizer;
    private final CoreUtilsFactory coreUtilsFactory;

    @AssistedInject
    private LeftJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalJoinCondition,
                             TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, IntermediateQueryFactory iqFactory,
                             ConditionSimplifier conditionSimplifier, LeftJoinNormalizer ljNormalizer,
                             JoinOrFilterVariableNullabilityTools variableNullabilityTools, CoreUtilsFactory coreUtilsFactory, IQTreeTools iqTreeTools) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, variableNullabilityTools, conditionSimplifier, iqTreeTools);
        this.ljNormalizer = ljNormalizer;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @AssistedInject
    private LeftJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                             TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory,
                             IntermediateQueryFactory iqFactory,
                             ConditionSimplifier conditionSimplifier, LeftJoinNormalizer ljNormalizer,
                             JoinOrFilterVariableNullabilityTools variableNullabilityTools, CoreUtilsFactory coreUtilsFactory, IQTreeTools iqTreeTools) {
        this(Optional.of(joiningCondition), nullabilityEvaluator, substitutionFactory,
                termFactory, typeFactory, iqFactory, conditionSimplifier, ljNormalizer, variableNullabilityTools, coreUtilsFactory, iqTreeTools);
    }

    @AssistedInject
    private LeftJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory,
                             IntermediateQueryFactory iqFactory,
                             ConditionSimplifier conditionSimplifier, LeftJoinNormalizer ljNormalizer,
                             JoinOrFilterVariableNullabilityTools variableNullabilityTools, CoreUtilsFactory coreUtilsFactory, IQTreeTools iqTreeTools) {
        this(Optional.empty(), nullabilityEvaluator, substitutionFactory,
                termFactory, typeFactory, iqFactory, conditionSimplifier, ljNormalizer, variableNullabilityTools, coreUtilsFactory, iqTreeTools);
    }

    @Override
    public int hashCode() {
        return getOptionalFilterCondition().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof LeftJoinNodeImpl) {
            LeftJoinNodeImpl that = (LeftJoinNodeImpl) o;
            return getOptionalFilterCondition().equals(that.getOptionalFilterCondition());
        }
        return false;
    }

    @Override
    public String toString() {
        return LEFT_JOIN_NODE_STR + getOptionalFilterString();
    }

    /**
     * Variable nullability for the full LJ tree
     */
    @Override
    public VariableNullability getVariableNullability(IQTree leftChild, IQTree rightChild) {

        /*
         * We apply the filter to the right (and then ignore it)
         */
        VariableNullability rightNullability = getOptionalFilterCondition()
                .map(c -> variableNullabilityTools.updateWithFilter(c, rightChild.getVariableNullability().getNullableGroups(),
                        rightChild.getVariables()))
                .orElseGet(rightChild::getVariableNullability);

        Set<Variable> rightSpecificVariables = Sets.difference(rightChild.getVariables(), leftChild.getVariables());

        ImmutableSet<ImmutableSet<Variable>> rightSelectedGroups = rightNullability.getNullableGroups().stream()
                .map(g -> Sets.intersection(g, rightSpecificVariables).immutableCopy())
                .filter(g -> !g.isEmpty())
                .collect(ImmutableCollectors.toSet());

        /*
         * New group for variables that can only become null due to the natural LJ
         */
        ImmutableSet<Variable> initiallyNonNullableRightSpecificGroup = rightSpecificVariables.stream()
                .filter(v -> !rightNullability.isPossiblyNullable(v))
                .collect(ImmutableCollectors.toSet());

        Set<ImmutableSet<Variable>> rightGroupStream = initiallyNonNullableRightSpecificGroup.isEmpty()
                ? rightSelectedGroups
                : Sets.union(ImmutableSet.of(initiallyNonNullableRightSpecificGroup), rightSelectedGroups);

        /*
         * Nullable groups from the left are preserved
         *
         * Nullable groups from the right are only dealing with right-specific variables
         */
        ImmutableSet<ImmutableSet<Variable>> nullableGroups = Sets.union(
                leftChild.getVariableNullability().getNullableGroups(), rightGroupStream).immutableCopy();

        ImmutableSet<Variable> scope = Sets.union(leftChild.getVariables(), rightChild.getVariables()).immutableCopy();

        return coreUtilsFactory.createVariableNullability(nullableGroups, scope);
    }

    /**
     * Returns possible definitions for left and right-specific variables.
     */
    @Override
    public ImmutableSet<Substitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree leftChild, IQTree rightChild) {
        ImmutableSet<Substitution<NonVariableTerm>> leftDefs = leftChild.getPossibleVariableDefinitions();

        Set<Variable> rightSpecificVariables = Sets.difference(rightChild.getVariables(), leftChild.getVariables());

        ImmutableSet<Substitution<NonVariableTerm>> rightDefs = rightChild.getPossibleVariableDefinitions().stream()
                .map(s -> s.restrictDomainTo(rightSpecificVariables))
                .collect(ImmutableCollectors.toSet());

        if (leftDefs.isEmpty())
            return rightDefs;

        if (rightDefs.isEmpty())
            return leftDefs;

        return leftDefs.stream()
                    .flatMap(l -> rightDefs.stream()
                            .map(r -> substitutionFactory.union(l, r)))
                    .collect(ImmutableCollectors.toSet());
    }


    @Override
    public IQTree normalizeForOptimization(IQTree initialLeftChild, IQTree initialRightChild, VariableGenerator variableGenerator,
                              IQTreeCache treeCache) {
        return ljNormalizer.normalizeForOptimization(this, initialLeftChild, initialRightChild,
                variableGenerator, treeCache);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree leftChild, IQTree rightChild,
                                              VariableGenerator variableGenerator) {
        if (leftChild.getVariables().contains(variable)) {
            IQTree liftedLeftChild = leftChild.liftIncompatibleDefinitions(variable, variableGenerator);
            var union = IQTreeTools.NaryIQTreeDecomposition.of(liftedLeftChild, UnionNode.class);
            if (union.isPresent()
                    && union.getNode().hasAChildWithLiftableDefinition(variable, union.getChildren())) {

                return iqTreeTools.createUnionTree(
                        iqTreeTools.getChildrenVariables(leftChild, rightChild),
                        union.getChildren().stream()
                                .<IQTree>map(c -> iqFactory.createBinaryNonCommutativeIQTree(this, c, rightChild))
                                .collect(ImmutableCollectors.toList()));
            }
        }

        // By default, nothing lifted
        return iqFactory.createBinaryNonCommutativeIQTree(this, leftChild, rightChild);
    }

    /**
     * NB: the constraint is only propagated to the left child
     */
    @Override
    public IQTree applyDescendingSubstitution(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild,
            VariableGenerator variableGenerator) {

        if (constraint
                .filter(c -> isRejectingRightSpecificNulls(c, leftChild, rightChild))
                .isPresent()
                || containsEqualityRightSpecificVariable(descendingSubstitution, leftChild, rightChild))
            return transformIntoInnerJoinTree(leftChild, rightChild)
                .applyDescendingSubstitution(descendingSubstitution, constraint, variableGenerator);

        IQTree updatedLeftChild = leftChild.applyDescendingSubstitution(descendingSubstitution, constraint, variableGenerator);

        Optional<ImmutableExpression> initialExpression = getOptionalFilterCondition();
        if (initialExpression.isPresent()) {
            try {
                ExpressionAndSubstitution expressionAndCondition = applyDescendingSubstitutionToExpression(
                        initialExpression.get(), descendingSubstitution, leftChild.getVariables(), rightChild.getVariables());

                Substitution<? extends VariableOrGroundTerm> rightDescendingSubstitution =
                        substitutionFactory.onVariableOrGroundTerms().compose(expressionAndCondition.getSubstitution(), descendingSubstitution);

                IQTree updatedRightChild = rightChild.applyDescendingSubstitution(rightDescendingSubstitution, Optional.empty(), variableGenerator);

                return updatedRightChild.isDeclaredAsEmpty()
                        ? updatedLeftChild
                        : iqFactory.createBinaryNonCommutativeIQTree(
                                iqFactory.createLeftJoinNode(expressionAndCondition.getOptionalExpression()),
                                updatedLeftChild, updatedRightChild);
            } catch (UnsatisfiableConditionException e) {
                return updatedLeftChild;
            }
        }
        else {
            IQTree updatedRightChild = rightChild.applyDescendingSubstitution(descendingSubstitution, Optional.empty(),
                    variableGenerator);
            if (updatedRightChild.isDeclaredAsEmpty()) {
                ImmutableSet<Variable> leftVariables = updatedLeftChild.getVariables();
                ImmutableSet<Variable> projectedVariables = Sets.union(leftVariables,
                        updatedRightChild.getVariables()).immutableCopy();

                Substitution<?> substitution = Sets.difference(projectedVariables, leftVariables).stream()
                        .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant()));

                var optionalConstructionNode = iqTreeTools.createOptionalConstructionNode(
                        () -> projectedVariables,
                        substitution);

                return iqTreeTools.createOptionalUnaryIQTree(
                        optionalConstructionNode,
                        updatedLeftChild);
            }
            return iqFactory.createBinaryNonCommutativeIQTree(this, updatedLeftChild, updatedRightChild);
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              IQTree leftChild, IQTree rightChild, VariableGenerator variableGenerator) {
        if (containsEqualityRightSpecificVariable(descendingSubstitution, leftChild, rightChild))
            return transformIntoInnerJoinTree(leftChild, rightChild)
                    .applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator);

        IQTree newLeftChild = leftChild.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator);
        IQTree newRightChild = rightChild.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator);

        LeftJoinNode newLJNode = getOptionalFilterCondition()
                .map(descendingSubstitution::apply)
                .map(iqFactory::createLeftJoinNode)
                .orElse(this);

        return iqFactory.createBinaryNonCommutativeIQTree(newLJNode, newLeftChild, newRightChild);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution, IQTree leftChild, IQTree rightChild, IQTreeCache treeCache) {
        IQTree newLeftChild = leftChild.applyFreshRenaming(renamingSubstitution);
        IQTree newRightChild = rightChild.applyFreshRenaming(renamingSubstitution);

        Optional<ImmutableExpression> newCondition = getOptionalFilterCondition()
                .map(renamingSubstitution::apply);

        LeftJoinNode newLeftJoinNode = newCondition.equals(getOptionalFilterCondition())
                ? this
                : iqFactory.createLeftJoinNode(newCondition);

        IQTreeCache newTreeCache = treeCache.applyFreshRenaming(renamingSubstitution);
        return iqFactory.createBinaryNonCommutativeIQTree(newLeftJoinNode, newLeftChild, newRightChild, newTreeCache);
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree leftChild, IQTree rightChild) {
        return Stream.of(leftChild, rightChild)
                .anyMatch(c -> c.isConstructed(variable));
    }

    /**
     * May check if the common
     */
    @Override
    public boolean isDistinct(IQTree tree, IQTree leftChild, IQTree rightChild) {
        if (!leftChild.isDistinct())
            return false;
        if (rightChild.isDistinct())
            return true;

        Optional<ImmutableExpression> optionalFilterCondition = getOptionalFilterCondition();

        ImmutableSet<Variable> rightVariables = rightChild.getVariables();
        Set<Variable> commonVariables = Sets.intersection(leftChild.getVariables(), rightVariables);

        if ((!optionalFilterCondition.isPresent()) && commonVariables.isEmpty())
            return false;

        ImmutableSet<ImmutableSet<Variable>> rightConstraints = rightChild.inferUniqueConstraints();
        if (rightConstraints.isEmpty())
            return false;

        // Common variables have an implicit IS_NOT_NULL condition
        ImmutableSet<ImmutableSet<Variable>> nullableGroups = rightChild.getVariableNullability().getNullableGroups().stream()
                .filter(g -> Sets.intersection(g, commonVariables).isEmpty())
                .collect(ImmutableCollectors.toSet());

        VariableNullability variableNullabilityForRight = optionalFilterCondition
                .map(c -> variableNullabilityTools.updateWithFilter(
                        optionalFilterCondition.get(), nullableGroups, rightVariables))
                .orElseGet(() -> coreUtilsFactory.createVariableNullability(nullableGroups, rightVariables));

        return rightConstraints.stream()
                .anyMatch(c -> c.stream().noneMatch(variableNullabilityForRight::isPossiblyNullable));
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild,
                                          VariableGenerator variableGenerator) {
        return propagateDownCondition(Optional.of(constraint), leftChild, rightChild, variableGenerator);
    }

    @Override
    public void validateNode(IQTree leftChild, IQTree rightChild) throws InvalidIntermediateQueryException {
        getOptionalFilterCondition()
                .ifPresent(e -> checkExpression(e, ImmutableList.of(leftChild, rightChild)));

        checkNonProjectedVariables(ImmutableList.of(leftChild, rightChild));
    }

    @Override
    public IQTree removeDistincts(IQTree leftChild, IQTree rightChild, IQTreeCache treeCache) {
        IQTree newLeftChild = leftChild.removeDistincts();
        IQTree newRightChild = rightChild.removeDistincts();

        IQTreeCache newTreeCache = treeCache.declareDistinctRemoval(newLeftChild.equals(leftChild) && newRightChild.equals(rightChild));
        return iqFactory.createBinaryNonCommutativeIQTree(this, newLeftChild, newRightChild, newTreeCache);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree leftChild, IQTree rightChild) {
        ImmutableSet<ImmutableSet<Variable>> leftChildConstraints = leftChild.inferUniqueConstraints();
        if (leftChildConstraints.isEmpty())
            return ImmutableSet.of();

        ImmutableSet<ImmutableSet<Variable>> rightChildConstraints = rightChild.inferUniqueConstraints();
        if (rightChildConstraints.isEmpty())
            return ImmutableSet.of();

        Set<Variable> commonVariables = Sets.intersection(leftChild.getVariables(), rightChild.getVariables());

        if (commonVariables.isEmpty() || rightChildConstraints.stream().noneMatch(commonVariables::containsAll))
            return ImmutableSet.of();

        return leftChildConstraints;
    }

    @Override
    public FunctionalDependencies inferFunctionalDependencies(IQTree leftChild, IQTree rightChild,
                                                              ImmutableSet<ImmutableSet<Variable>> uniqueConstraints,
                                                              ImmutableSet<Variable> variables) {
        FunctionalDependencies rightFunctionalDependencies = rightChild.inferFunctionalDependencies();
        if (rightFunctionalDependencies.isEmpty())
            return leftChild.inferFunctionalDependencies();

        ImmutableSet<Variable> leftVariables = leftChild.getVariables();

        // Makes sure the right child does not add FDs on left variables (as dependents)
        FunctionalDependencies filterRightFunctionalDependencies = rightFunctionalDependencies.stream()
                .map(e -> Maps.immutableEntry(
                        e.getKey(),
                        Sets.difference(e.getValue(), leftVariables).immutableCopy()))
                .filter(e -> !e.getValue().isEmpty())
                .collect(FunctionalDependencies.toFunctionalDependencies());

        if (filterRightFunctionalDependencies.isEmpty())
            return leftChild.inferFunctionalDependencies();

        return leftChild.inferFunctionalDependencies()
                .concat(filterRightFunctionalDependencies);
    }

    @Override
    public ImmutableSet<Variable> inferStrictDependents(BinaryNonCommutativeIQTree tree, IQTree leftChild, IQTree rightChild) {
        return IQTreeTools.computeStrictDependentsFromFunctionalDependencies(tree);
    }

    @Override
    public VariableNonRequirement computeNotInternallyRequiredVariables(IQTree leftChild, IQTree rightChild) {
        return computeVariableNonRequirement(ImmutableList.of(leftChild, rightChild));
    }

    /**
     * Can propagate on the left, but not on the right.
     *
     * Transforms the left join into an inner join when the constraint is rejecting nulls from the right
     */
    private IQTree propagateDownCondition(Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild,
                                          VariableGenerator variableGenerator) {

        if (constraint
                .filter(c -> isRejectingRightSpecificNulls(c, leftChild, rightChild))
                .isPresent())
            return transformIntoInnerJoinTree(leftChild, rightChild)
                    .propagateDownConstraint(constraint.get(), variableGenerator);

        IQTree newLeftChild = iqTreeTools.propagateDownOptionalConstraint(leftChild, constraint, variableGenerator);
        return iqFactory.createBinaryNonCommutativeIQTree(this, newLeftChild, rightChild);
    }

    private ExpressionAndSubstitution applyDescendingSubstitutionToExpression(
            ImmutableExpression initialExpression,
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableSet<Variable> leftChildVariables, ImmutableSet<Variable> rightChildVariables)
            throws UnsatisfiableConditionException {

        ImmutableExpression expression = descendingSubstitution.apply(initialExpression);
        // No proper variable nullability information is given for optimizing during descending substitution
        // (too complicated)
        // Therefore, please consider normalizing afterwards
        ImmutableExpression.Evaluation results = expression.evaluate2VL(
                coreUtilsFactory.createSimplifiedVariableNullability(expression));

        if (results.isEffectiveFalse())
            throw new UnsatisfiableConditionException();

        return results.getExpression()
                .map(e -> convertIntoExpressionAndSubstitution(e, leftChildVariables, rightChildVariables))
                .orElseGet(() ->
                        new ExpressionAndSubstitutionImpl(Optional.empty(), descendingSubstitution.restrictRangeTo(VariableOrGroundTerm.class)));
    }

    /**
     * TODO: explain
     *
     */
    private ExpressionAndSubstitution convertIntoExpressionAndSubstitution(ImmutableExpression expression,
                                                                           ImmutableSet<Variable> leftVariables,
                                                                           ImmutableSet<Variable> rightVariables) {

        Set<Variable> rightSpecificVariables = Sets.difference(rightVariables, leftVariables);

        ImmutableSet<ImmutableExpression> expressions = expression.flattenAND()
                .collect(ImmutableCollectors.toSet());
        ImmutableSet<ImmutableExpression> downSubstitutionExpressions = expressions.stream()
                .filter(e -> e.getFunctionSymbol() instanceof DBStrictEqFunctionSymbol)
                // TODO: refactor it for dealing with n-ary EQs
                .filter(e -> {
                    ImmutableList<? extends ImmutableTerm> arguments = e.getTerms();
                    return arguments.stream().allMatch(t -> t instanceof NonFunctionalTerm)
                            && arguments.stream().anyMatch(rightVariables::contains);
                })
                .collect(ImmutableCollectors.toSet());

        Substitution<VariableOrGroundTerm> downSubstitution = downSubstitutionExpressions.stream()
                        .map(ImmutableFunctionalTerm::getTerms)
                        .map(args -> (args.get(0) instanceof Variable) ? args : args.reverse())
                        // Rename right-specific variables if possible
                        .map(args -> ((args.get(0) instanceof Variable) && rightSpecificVariables.contains(args.get(1)))
                                ? args.reverse() : args)
                        .collect(substitutionFactory.toSubstitution(
                                args -> (Variable) args.get(0),
                                args -> (VariableOrGroundTerm) args.get(1)));

        Optional<ImmutableExpression> newExpression = Optional.of(expressions.stream()
                        .filter(e -> !downSubstitutionExpressions.contains(e)
                                || e.getTerms().stream().anyMatch(rightSpecificVariables::contains))
                        .collect(ImmutableCollectors.toList()))
                .filter(l -> !l.isEmpty())
                .map(termFactory::getConjunction)
                .map(downSubstitution::apply);

        return new ExpressionAndSubstitutionImpl(newExpression, downSubstitution);
    }

    private boolean isRejectingRightSpecificNulls(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild) {

        Set<Variable> nullVariables = Sets.intersection(
                Sets.difference(rightChild.getVariables(), leftChild.getVariables()),
                constraint.getVariables());

        if (nullVariables.isEmpty())
            return false;

        ImmutableExpression nullifiedExpression = nullVariables.stream()
                .collect(substitutionFactory.toSubstitution(v -> termFactory.getNullConstant()))
                .apply(constraint);

        return nullifiedExpression.evaluate2VL(termFactory.createDummyVariableNullability(nullifiedExpression))
                .isEffectiveFalse();
    }

    /**
     * Returns true when an equality between a right-specific and a term that is not a fresh variable
     * is propagated down through a substitution.
     */
    private boolean containsEqualityRightSpecificVariable(
            Substitution<? extends VariableOrGroundTerm> descendingSubstitution,
            IQTree leftChild, IQTree rightChild) {

        ImmutableSet<Variable> leftVariables = leftChild.getVariables();
        ImmutableSet<Variable> rightVariables = rightChild.getVariables();

        Substitution<Variable> restricted = descendingSubstitution.restrictRangeTo(Variable.class);

        Set<Variable> variables = Sets.union(leftVariables, rightVariables);
        ImmutableSet<Variable> freshVariables = restricted.getPreImage(t -> !variables.contains(t));

        return !Sets.intersection(
                        Sets.difference(rightVariables, leftVariables),
                        Sets.union(
                                // The domain of the substitution is assumed not to contain fresh variables (normalized before)
                                Sets.difference(descendingSubstitution.getDomain(), freshVariables),
                                restricted.getRangeSet()))
                .isEmpty();
    }

    private IQTree transformIntoInnerJoinTree(IQTree leftChild, IQTree rightChild) {
        return iqTreeTools.createInnerJoinTree(
                getOptionalFilterCondition(),
                ImmutableList.of(leftChild, rightChild));
    }

    @Override
    protected VariableNonRequirement applyFilterToVariableNonRequirement(VariableNonRequirement nonRequirementBeforeFilter,
                                                                         ImmutableList<IQTree> children) {

        if (nonRequirementBeforeFilter.isEmpty())
            return nonRequirementBeforeFilter;

        IQTree leftChild = children.get(0);
        IQTree rightChild = children.get(1);

        Set<Variable> rightSpecificVariables = Sets.difference(rightChild.getVariables(), leftChild.getVariables());

        if (rightSpecificVariables.isEmpty())
            return nonRequirementBeforeFilter;

        Set<Variable> commonVariables = Sets.intersection(leftChild.getVariables(), rightChild.getVariables());

        /*
         * If the right child has no impact on cardinality (i.e. at most one match per row on the left),
         *  it can potentially be eliminated if no right-specific variables is used above the LJ.
         *
         * Not required variables (before the LJ condition) that are involved in the LJ condition can be eliminated
         *   if all the right-specific variables are removed too.
         */
        if ((!commonVariables.isEmpty())
                && rightChild.inferUniqueConstraints().stream()
                    .anyMatch(commonVariables::containsAll)) {

            Set<Variable> rightSpecificNonRequiredVariables = Sets.intersection(
                    rightSpecificVariables, nonRequirementBeforeFilter.getNotRequiredVariables());

            ImmutableSet<Variable> filterVariables = getLocalVariables();

            return nonRequirementBeforeFilter.transformConditions(
                    (v, conditions) -> filterVariables.contains(v)
                            ? Sets.union(conditions, rightSpecificNonRequiredVariables).immutableCopy()
                            : conditions);
        }
        else
            return super.applyFilterToVariableNonRequirement(nonRequirementBeforeFilter, children);
    }
}
