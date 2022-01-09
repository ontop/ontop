package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.LeftJoinNormalizer;
import it.unibz.inf.ontop.iq.node.normalization.impl.ExpressionAndSubstitutionImpl;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
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
                             ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                             ConditionSimplifier conditionSimplifier, LeftJoinNormalizer ljNormalizer,
                             JoinOrFilterVariableNullabilityTools variableNullabilityTools, CoreUtilsFactory coreUtilsFactory) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, unificationTools, substitutionTools, variableNullabilityTools, conditionSimplifier);
        this.ljNormalizer = ljNormalizer;
        this.coreUtilsFactory = coreUtilsFactory;
    }

    @AssistedInject
    private LeftJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                             TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory,
                             IntermediateQueryFactory iqFactory, ImmutableUnificationTools unificationTools,
                             ImmutableSubstitutionTools substitutionTools, ConditionSimplifier conditionSimplifier, LeftJoinNormalizer ljNormalizer,
                             JoinOrFilterVariableNullabilityTools variableNullabilityTools, CoreUtilsFactory coreUtilsFactory) {
        this(Optional.of(joiningCondition), nullabilityEvaluator, substitutionFactory,
                termFactory, typeFactory, iqFactory, unificationTools, substitutionTools, conditionSimplifier, ljNormalizer, variableNullabilityTools, coreUtilsFactory);
    }

    @AssistedInject
    private LeftJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory,
                             IntermediateQueryFactory iqFactory, ImmutableUnificationTools unificationTools,
                             ImmutableSubstitutionTools substitutionTools, ConditionSimplifier conditionSimplifier, LeftJoinNormalizer ljNormalizer,
                             JoinOrFilterVariableNullabilityTools variableNullabilityTools, CoreUtilsFactory coreUtilsFactory) {
        this(Optional.empty(), nullabilityEvaluator, substitutionFactory,
                termFactory, typeFactory, iqFactory, unificationTools, substitutionTools, conditionSimplifier, ljNormalizer, variableNullabilityTools, coreUtilsFactory);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public LeftJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public LeftJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new LeftJoinNodeImpl(newOptionalFilterCondition, nullabilityEvaluator, substitutionFactory,
                termFactory, typeFactory, iqFactory,
                unificationTools, substitutionTools, conditionSimplifier, ljNormalizer, variableNullabilityTools, coreUtilsFactory);
    }

    @Override
    public int hashCode() {
        return getOptionalFilterCondition().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass()
                && getOptionalFilterCondition().equals(((LeftJoinNode) obj).getOptionalFilterCondition());
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

        ImmutableSet<Variable> rightSpecificVariables = Sets.difference(rightChild.getVariables(), leftChild.getVariables())
                .immutableCopy();

        ImmutableSet<ImmutableSet<Variable>> rightSelectedGroups = rightNullability.getNullableGroups().stream()
                .map(g -> g.stream()
                        .filter(rightSpecificVariables::contains)
                        .collect(ImmutableCollectors.toSet()))
                .filter(g -> !g.isEmpty())
                .collect(ImmutableCollectors.toSet());

        /*
         * New group for variables that can only become null due to the natural LJ
         */
        ImmutableSet<Variable> initiallyNonNullableRightSpecificGroup = rightSpecificVariables.stream()
                .filter(v -> !rightNullability.isPossiblyNullable(v))
                .collect(ImmutableCollectors.toSet());

        Stream<ImmutableSet<Variable>> rightGroupStream = initiallyNonNullableRightSpecificGroup.isEmpty()
                ? rightSelectedGroups.stream()
                : Stream.concat(Stream.of(initiallyNonNullableRightSpecificGroup), rightSelectedGroups.stream());

        /*
         * Nullable groups from the left are preserved
         *
         * Nullable groups from the right are only dealing with right-specific variables
         */
        ImmutableSet<ImmutableSet<Variable>> nullableGroups = Stream.concat(
                leftChild.getVariableNullability().getNullableGroups().stream(),
                rightGroupStream)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> scope = Sets.union(leftChild.getVariables(), rightChild.getVariables()).immutableCopy();

        return coreUtilsFactory.createVariableNullability(nullableGroups, scope);
    }

    /**
     * Returns possible definitions for left and right-specific variables.
     */
    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree leftChild, IQTree rightChild) {
        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> leftDefs = leftChild.getPossibleVariableDefinitions();

        ImmutableSet<Variable> rightSpecificVariables = Sets.difference(rightChild.getVariables(), leftChild.getVariables())
                .immutableCopy();

        ImmutableSet<ImmutableSubstitution<NonVariableTerm>> rightDefs = rightChild.getPossibleVariableDefinitions().stream()
                .map(s -> s.filter(rightSpecificVariables::contains))
                .collect(ImmutableCollectors.toSet());

        if (leftDefs.isEmpty())
            return rightDefs;

        if (rightDefs.isEmpty())
            return leftDefs;

        return leftDefs.stream()
                    .flatMap(l -> rightDefs.stream()
                            .map(r -> l.union(r).orElseThrow(() -> new MinorOntopInternalBugException(
                                                    "Unexpected conflict between " + l + " and " + r))))
                    .collect(ImmutableCollectors.toSet());
    }


    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree leftChild, IQTree rightChild) {
        return transformer.transformLeftJoin(tree,this, leftChild, rightChild);
    }

    @Override
    public <T> IQTree acceptTransformer(IQTree tree, IQTreeExtendedTransformer<T> transformer, IQTree leftChild,
                                    IQTree rightChild, T context) {
        return transformer.transformLeftJoin(tree,this, leftChild, rightChild, context);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree leftChild, IQTree rightChild) {
        return visitor.visitLeftJoin(this, leftChild, rightChild);
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
            QueryNode leftChildRoot = liftedLeftChild.getRootNode();

            if (leftChildRoot instanceof UnionNode
                    && ((UnionNode) leftChildRoot).hasAChildWithLiftableDefinition(variable, liftedLeftChild.getChildren())) {

                UnionNode newUnionNode = iqFactory.createUnionNode(
                        Stream.of(leftChild, rightChild)
                                .flatMap(c -> c.getVariables().stream())
                                .collect(ImmutableCollectors.toSet()));

                return iqFactory.createNaryIQTree(newUnionNode,
                        liftedLeftChild.getChildren().stream()
                        .map(unionChild -> (IQTree) iqFactory.createBinaryNonCommutativeIQTree(this, unionChild, rightChild))
                        .collect(ImmutableCollectors.toList()));
            }
        }

        // By default, nothing lifted
        return iqFactory.createBinaryNonCommutativeIQTree(this, leftChild, rightChild);

    }

    /**
     * NB: the constraint is only propagate to the left child
     */
    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild) {

        if (constraint
                .filter(c -> isRejectingRightSpecificNulls(c, leftChild, rightChild))
                .isPresent()
                || containsEqualityRightSpecificVariable(descendingSubstitution, leftChild, rightChild))
            return transformIntoInnerJoinTree(leftChild, rightChild)
                .applyDescendingSubstitution(descendingSubstitution, constraint);

        IQTree updatedLeftChild = leftChild.applyDescendingSubstitution(descendingSubstitution, constraint);

        Optional<ImmutableExpression> initialExpression = getOptionalFilterCondition();
        if (initialExpression.isPresent()) {
            try {
                ExpressionAndSubstitution expressionAndCondition = applyDescendingSubstitutionToExpression(
                        initialExpression.get(), descendingSubstitution, leftChild.getVariables(), rightChild.getVariables());

                // TODO: remove the casts
                ImmutableSubstitution<? extends VariableOrGroundTerm> rightDescendingSubstitution =
                        expressionAndCondition.getSubstitution().composeWith2(descendingSubstitution);

                IQTree updatedRightChild = rightChild.applyDescendingSubstitution(rightDescendingSubstitution, Optional.empty());

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
            IQTree updatedRightChild = rightChild.applyDescendingSubstitution(descendingSubstitution, Optional.empty());
            if (updatedRightChild.isDeclaredAsEmpty()) {
                ImmutableSet<Variable> leftVariables = updatedLeftChild.getVariables();
                ImmutableSet<Variable> projectedVariables = Sets.union(leftVariables,
                        updatedRightChild.getVariables()).immutableCopy();

                Optional<ConstructionNode> constructionNode = Optional.of(projectedVariables)
                        .filter(vars -> !leftVariables.containsAll(vars))
                        .map(vars -> substitutionFactory.getNullSubstitution(
                                projectedVariables.stream()
                                        .filter(v -> !leftVariables.contains(v))))
                        .map(s -> iqFactory.createConstructionNode(projectedVariables, s));

                return constructionNode
                        .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, updatedLeftChild))
                        .orElse(updatedLeftChild);
            }
            return iqFactory.createBinaryNonCommutativeIQTree(this, updatedLeftChild, updatedRightChild);
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              IQTree leftChild, IQTree rightChild) {
        if (containsEqualityRightSpecificVariable(descendingSubstitution, leftChild, rightChild))
            return transformIntoInnerJoinTree(leftChild, rightChild)
                    .applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution);

        IQTree newLeftChild = leftChild.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution);
        IQTree newRightChild = rightChild.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution);

        LeftJoinNode newLJNode = getOptionalFilterCondition()
                .map(descendingSubstitution::applyToBooleanExpression)
                .map(iqFactory::createLeftJoinNode)
                .orElse(this);

        return iqFactory.createBinaryNonCommutativeIQTree(newLJNode, newLeftChild, newRightChild);
    }

    @Override
    public IQTree applyFreshRenaming(InjectiveVar2VarSubstitution renamingSubstitution, IQTree leftChild, IQTree rightChild, IQTreeCache treeCache) {
        IQTree newLeftChild = leftChild.applyFreshRenaming(renamingSubstitution);
        IQTree newRightChild = rightChild.applyFreshRenaming(renamingSubstitution);

        Optional<ImmutableExpression> newCondition = getOptionalFilterCondition()
                .map(renamingSubstitution::applyToBooleanExpression);

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

        ImmutableSet<Variable> leftVariables = leftChild.getVariables();
        ImmutableSet<Variable> rightVariables = rightChild.getVariables();
        Sets.SetView<Variable> commonVariables = Sets.intersection(leftVariables, rightVariables);

        if ((!optionalFilterCondition.isPresent()) && commonVariables.isEmpty())
            return false;

        ImmutableSet<ImmutableSet<Variable>> rightConstraints = rightChild.inferUniqueConstraints();
        if (rightConstraints.isEmpty())
            return false;

        // Common variables have an implicit IS_NOT_NULL condition
        ImmutableSet<ImmutableSet<Variable>> nullableGroups = rightChild.getVariableNullability().getNullableGroups().stream()
                .filter(g -> g.stream().noneMatch(commonVariables::contains))
                .collect(ImmutableCollectors.toSet());

        VariableNullability variableNullabilityForRight = optionalFilterCondition
                .map(c -> variableNullabilityTools.updateWithFilter(
                        optionalFilterCondition.get(), nullableGroups, rightVariables))
                .orElseGet(() -> coreUtilsFactory.createVariableNullability(nullableGroups, rightVariables));

        return rightConstraints.stream()
                .anyMatch(c -> c.stream().noneMatch(variableNullabilityForRight::isPossiblyNullable));
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild) {
        return propagateDownCondition(Optional.of(constraint), leftChild, rightChild);
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

    /**
     * TODO: implement it seriously
     */
    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree leftChild, IQTree rightChild) {
        return ImmutableSet.of();
    }

    @Override
    public ImmutableSet<Variable> computeNotInternallyRequiredVariables(IQTree leftChild, IQTree rightChild) {
        return computeNotInternallyRequiredVariables(ImmutableList.of(leftChild, rightChild));
    }

    /**
     * Can propagate on the left, but not on the right.
     *
     * Transforms the left join into an inner join when the constraint is rejecting nulls from the right
     */
    private IQTree propagateDownCondition(Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild) {

        if (constraint
                .filter(c -> isRejectingRightSpecificNulls(c, leftChild, rightChild))
                .isPresent())
            return transformIntoInnerJoinTree(leftChild, rightChild)
                    .propagateDownConstraint(constraint.get());

        IQTree newLeftChild = constraint
                .map(leftChild::propagateDownConstraint)
                .orElse(leftChild);
        return iqFactory.createBinaryNonCommutativeIQTree(this, newLeftChild, rightChild);
    }

    private ExpressionAndSubstitution applyDescendingSubstitutionToExpression(
            ImmutableExpression initialExpression,
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableSet<Variable> leftChildVariables, ImmutableSet<Variable> rightChildVariables)
            throws UnsatisfiableConditionException {

        ImmutableExpression expression = descendingSubstitution.applyToBooleanExpression(initialExpression);
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
                        new ExpressionAndSubstitutionImpl(Optional.empty(), descendingSubstitution.getFragment(VariableOrGroundTerm.class)));
    }

    /**
     * TODO: explain
     *
     */
    private ExpressionAndSubstitution convertIntoExpressionAndSubstitution(ImmutableExpression expression,
                                                                           ImmutableSet<Variable> leftVariables,
                                                                           ImmutableSet<Variable> rightVariables) {

        ImmutableSet<Variable> rightSpecificVariables = rightVariables.stream()
                .filter(v -> !leftVariables.contains(v))
                .collect(ImmutableCollectors.toSet());


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

        ImmutableSubstitution<VariableOrGroundTerm> downSubstitution =
                substitutionFactory.getSubstitution(
                        downSubstitutionExpressions.stream()
                            .map(ImmutableFunctionalTerm::getTerms)
                            .map(args -> (args.get(0) instanceof Variable) ? args : args.reverse())
                            // Rename right-specific variables if possible
                            .map(args -> ((args.get(0) instanceof Variable) && rightSpecificVariables.contains(args.get(1)))
                                    ? args.reverse() : args)
                            .collect(ImmutableCollectors.toMap(
                                    args -> (Variable) args.get(0),
                                    args -> (VariableOrGroundTerm) args.get(1))));

        Optional<ImmutableExpression> newExpression = Optional.of(expressions.stream()
                        .filter(e -> !downSubstitutionExpressions.contains(e)
                                || e.getTerms().stream().anyMatch(rightSpecificVariables::contains))
                        .collect(ImmutableCollectors.toList()))
                .filter(l -> !l.isEmpty())
                .map(termFactory::getConjunction)
                .map(downSubstitution::applyToBooleanExpression);

        return new ExpressionAndSubstitutionImpl(newExpression, downSubstitution);
    }

    private boolean isRejectingRightSpecificNulls(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild) {
        ImmutableSet<Variable> constraintVariables = constraint.getVariables();

        ImmutableSet<Variable> nullVariables = Sets.difference(rightChild.getVariables(), leftChild.getVariables()).stream()
                .filter(constraintVariables::contains)
                .collect(ImmutableCollectors.toSet());

        if (nullVariables.isEmpty())
            return false;

        ImmutableExpression nullifiedExpression = substitutionFactory.getNullSubstitution(nullVariables.stream())
                .applyToBooleanExpression(constraint);

        return nullifiedExpression.evaluate2VL(termFactory.createDummyVariableNullability(nullifiedExpression))
                .isEffectiveFalse();
    }

    /**
     * Returns true when an equality between a right-specific and a term that is not a fresh variable
     * is propagated down through a substitution.
     */
    private boolean containsEqualityRightSpecificVariable(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            IQTree leftChild, IQTree rightChild) {

        ImmutableSet<Variable> leftVariables = leftChild.getVariables();
        ImmutableSet<Variable> rightVariables = rightChild.getVariables();
        ImmutableSet<Variable> domain = descendingSubstitution.getDomain();
        ImmutableCollection<? extends VariableOrGroundTerm> range = descendingSubstitution.getImmutableMap().values();

        return rightVariables.stream()
                .filter(v -> !leftVariables.contains(v))
                .anyMatch(v -> (domain.contains(v)
                            && (!isFreshVariable(descendingSubstitution.get(v), leftVariables, rightVariables)))
                        // The domain of the substitution is assumed not to contain fresh variables
                        // (normalized before)
                        || range.contains(v));
    }

    private boolean isFreshVariable(ImmutableTerm term,
                                    ImmutableSet<Variable> leftVariables, ImmutableSet<Variable> rightVariables) {
        if (term instanceof Variable) {
            Variable variable = (Variable) term;
            return !(leftVariables.contains(variable) || rightVariables.contains(variable));
        }
        return false;
    }

    private IQTree transformIntoInnerJoinTree(IQTree leftChild, IQTree rightChild) {
        return iqFactory.createNaryIQTree(
                iqFactory.createInnerJoinNode(getOptionalFilterCondition()),
                ImmutableList.of(leftChild, rightChild));
    }

}
