package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.impl.DefaultSubstitutionResults;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.DECLARE_AS_EMPTY;
import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.REPLACE_BY_NEW_NODE;
import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.REPLACE_BY_UNIQUE_NON_EMPTY_CHILD;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.EQ;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.IF_ELSE_NULL;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.IS_NOT_NULL;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class LeftJoinNodeImpl extends JoinLikeNodeImpl implements LeftJoinNode {


    private static final int MAX_ITERATIONS = 10000;

    private enum Provenance {
        FROM_ABOVE,
        FROM_LEFT,
        FROM_RIGHT
    }



    private static final String LEFT_JOIN_NODE_STR = "LJ";
    private final ImmutabilityTools immutabilityTools;
    private final ValueConstant valueNull;

    @AssistedInject
    private LeftJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalJoinCondition,
                             TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                             ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                             IntermediateQueryFactory iqFactory,
                             ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools, defaultExpressionEvaluator,
                immutabilityTools, substitutionFactory, unificationTools, substitutionTools);
        this.valueNull = termFactory.getNullConstant();
        this.immutabilityTools = immutabilityTools;
    }

    @AssistedInject
    private LeftJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                             TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                             ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                             IntermediateQueryFactory iqFactory, ImmutableUnificationTools unificationTools,
                             ImmutableSubstitutionTools substitutionTools) {
        super(Optional.of(joiningCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools, substitutionFactory, unificationTools, substitutionTools);
        this.valueNull = termFactory.getNullConstant();
        this.immutabilityTools = immutabilityTools;
    }

    @AssistedInject
    private LeftJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                             ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                             IntermediateQueryFactory iqFactory, ImmutableUnificationTools unificationTools,
                             ImmutableSubstitutionTools substitutionTools) {
        super(Optional.empty(), nullabilityEvaluator, termFactory, iqFactory, typeFactory, datalogTools, defaultExpressionEvaluator,
                immutabilityTools, substitutionFactory, unificationTools, substitutionTools);
        this.valueNull = termFactory.getNullConstant();
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public LeftJoinNode clone() {
        return new LeftJoinNodeImpl(getOptionalFilterCondition(), getNullabilityEvaluator(), substitutionFactory,
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), immutabilityTools, iqFactory,
                unificationTools, substitutionTools);
    }

    @Override
    public LeftJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public LeftJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new LeftJoinNodeImpl(newOptionalFilterCondition, getNullabilityEvaluator(), substitutionFactory,
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), immutabilityTools, iqFactory,
                unificationTools, substitutionTools);
    }

    @Override
    public SubstitutionResults<LeftJoinNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return  isFromRightBranch(childNode, query)
                ? applyAscendingSubstitutionFromRight(substitution, query, childNode)
                : applyAscendingSubstitutionFromLeft(substitution, query);
    }

    /**
     * Currently blocks the substitution coming from the right
     *
     * TODO: propagate NULLs
     */
    private SubstitutionResults<LeftJoinNode> applyAscendingSubstitutionFromRight(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query,
            QueryNode rightChild) {

        if (substitution.isEmpty()) {
            return DefaultSubstitutionResults.noChange();
        }
        else {
            ImmutableSet<Variable> rightProjectedVariables =
                    Stream.concat(
                            query.getVariables(rightChild).stream(),
                            substitution.getDomain().stream())
                    .collect(ImmutableCollectors.toSet());

            ConstructionNode newConstructionNode = iqFactory.createConstructionNode(rightProjectedVariables,
                    (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>) substitution);

            return DefaultSubstitutionResults.insertConstructionNode(newConstructionNode, rightChild);
        }
    }

    private SubstitutionResults<LeftJoinNode> applyAscendingSubstitutionFromLeft(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        QueryNode rightChild = query.getChild(this, RIGHT)
                .orElseThrow(() -> new IllegalStateException("No right child for the LJ"));
        ImmutableSet<Variable> rightVariables = query.getVariables(rightChild);

        /*
         * If the substitution will set some right variables to be null
         *  -> remove the right part
         */
        if (rightVariables.stream()
                .filter(substitution::isDefining)
                .anyMatch(v -> substitution.get(v).equals(valueNull))) {
            return proposeToRemoveTheRightPart(query, substitution, Optional.of(rightVariables), Provenance.FROM_LEFT);
        }

        /*
         * Updates the joining conditions (may add new equalities)
         * and propagates the same substitution if the conditions still holds.
         *
         */
        return computeAndEvaluateNewCondition(substitution, Optional.empty())
                .map(ev -> applyEvaluation(query, ev, substitution, Optional.of(rightVariables), Provenance.FROM_LEFT))
                .orElseGet(() -> DefaultSubstitutionResults.newNode(this, substitution));
    }


    @Override
    public SubstitutionResults<LeftJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {

        return getOptionalFilterCondition()
                .map(cond -> transformBooleanExpression(substitution, cond))
                .map(ev -> applyEvaluation(query, ev, substitution, Optional.empty(), Provenance.FROM_ABOVE))
                .orElseGet(() -> DefaultSubstitutionResults.noChange(substitution));
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        QueryNode leftChild = query.getChild(this, LEFT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A left child is required"));

        if (query.getVariables(leftChild).contains(variable))
            return leftChild.isVariableNullable(query, variable);

        QueryNode rightChild = query.getChild(this, RIGHT)
                .orElseThrow(() -> new InvalidIntermediateQueryException("A right child is required"));

        if (!query.getVariables(rightChild).contains(variable))
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);

        return false;
    }

    private SubstitutionResults<LeftJoinNode> applyEvaluation(IntermediateQuery query, ExpressionEvaluator.EvaluationResult evaluationResult,
                                                              ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                                              Optional<ImmutableSet<Variable>> optionalVariablesFromOppositeSide,
                                                              Provenance provenance) {
        /*
         * Joining condition does not hold: replace the LJ by its left child.
         */
        if (evaluationResult.isEffectiveFalse()) {
            return proposeToRemoveTheRightPart(query, substitution, optionalVariablesFromOppositeSide, provenance);
        }
        else {
            LeftJoinNode newNode = changeOptionalFilterCondition(evaluationResult.getOptionalExpression());
            return DefaultSubstitutionResults.newNode(newNode, substitution);
        }
    }

    private SubstitutionResults<LeftJoinNode> proposeToRemoveTheRightPart(
            IntermediateQuery query, ImmutableSubstitution<? extends ImmutableTerm> substitution,
            Optional<ImmutableSet<Variable>> optionalVariablesFromOppositeSide, Provenance provenance) {

        ImmutableSubstitution<? extends ImmutableTerm> newSubstitution;
        switch(provenance) {
            case FROM_LEFT:
                newSubstitution = removeRightChildSubstitutionFromLeft(query, substitution,
                        optionalVariablesFromOppositeSide);
                break;
            case FROM_RIGHT:
                newSubstitution = removeRightChildSubstitutionFromRight(query, substitution,
                        optionalVariablesFromOppositeSide);
                break;
            default:
                newSubstitution = substitution;
                break;
        }

        return DefaultSubstitutionResults.replaceByChild(newSubstitution, LEFT);
    }


    private ImmutableSubstitution<ImmutableTerm> removeRightChildSubstitutionFromLeft(
            IntermediateQuery query, ImmutableSubstitution<? extends ImmutableTerm> substitution,
            Optional<ImmutableSet<Variable>> optionalRightVariables) {

        ImmutableSet<Variable> leftVariables = query.getVariables(query.getChild(this, LEFT)
                .orElseThrow(() -> new IllegalStateException("Missing left child ")));
        ImmutableSet<Variable> rightVariables = getChildProjectedVariables(query, optionalRightVariables, RIGHT);

        ImmutableMap<Variable, ? extends ImmutableTerm> substitutionMap = substitution.getImmutableMap();

        ImmutableSet<Variable> newlyNullVariables = rightVariables.stream()
                .filter(v -> !leftVariables.contains(v))
                .filter(v -> !substitutionMap.containsKey(v))
                .collect(ImmutableCollectors.toSet());

        Stream<Map.Entry<Variable, ImmutableTerm>> nullEntries = newlyNullVariables.stream()
                .map(v -> new SimpleEntry<>(v, valueNull));

        Stream<Map.Entry<Variable, ImmutableTerm>> alreadyExistingEntries = substitution.getImmutableMap().entrySet().stream()
                .map(e -> (Map.Entry<Variable, ImmutableTerm>)e);

        return substitutionFactory.getSubstitution(
                Stream.concat(nullEntries, alreadyExistingEntries)
                        .collect(ImmutableCollectors.toMap()));


    }

    private ImmutableSubstitution<ImmutableTerm> removeRightChildSubstitutionFromRight(
            IntermediateQuery query, ImmutableSubstitution<? extends ImmutableTerm> substitution,
            Optional<ImmutableSet<Variable>> optionalLeftVariables) {

        ImmutableSet<Variable> leftVariables = getChildProjectedVariables(query, optionalLeftVariables, LEFT);
        ImmutableSet<Variable> rightVariables = query.getVariables(query.getChild(this, RIGHT)
                                .orElseThrow(() -> new IllegalStateException("Missing right child ")));

        ImmutableSet<Variable> newlyNullVariables = rightVariables.stream()
                .filter(v -> !leftVariables.contains(v))
                .collect(ImmutableCollectors.toSet());

        Stream<Map.Entry<Variable, ImmutableTerm>> nullEntries = newlyNullVariables.stream()
                .map(v -> new SimpleEntry<>(v, valueNull));

        Stream<Map.Entry<Variable, ImmutableTerm>> otherEntries = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> !newlyNullVariables.contains(e.getKey()))
                .map(e -> (Map.Entry<Variable, ImmutableTerm>)e);

        return substitutionFactory.getSubstitution(
                Stream.concat(nullEntries, otherEntries)
                        .collect(ImmutableCollectors.toMap()));
    }

    private ImmutableSet<Variable> getChildProjectedVariables(IntermediateQuery query,
                                                              Optional<ImmutableSet<Variable>> optionalChildVariables,
                                                              ArgumentPosition position) {
        return optionalChildVariables
                .orElseGet(() -> query.getVariables(query.getChild(this, position)
                                .orElseThrow(() -> new IllegalStateException("Missing child "))));
    }


    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof LeftJoinNode)
                && ((LeftJoinNode) node).getOptionalFilterCondition().equals(this.getOptionalFilterCondition());
    }

    @Override
    public NodeTransformationProposal reactToEmptyChild(IntermediateQuery query, EmptyNode emptyChild) {
        ArgumentPosition emptyNodePosition = query.getOptionalPosition(this, emptyChild)
                .orElseThrow(() -> new IllegalStateException("The deleted child of a LJ must have a position"));

        QueryNode otherChild = query.getChild(this, (emptyNodePosition == LEFT) ? RIGHT : LEFT)
                .orElseThrow(() -> new IllegalStateException("The other child of a LJ is missing"));

        ImmutableSet<Variable> variablesProjectedByOtherChild = query.getVariables(otherChild);

        ImmutableSet<Variable> nullVariables;

        switch(emptyNodePosition) {
            case LEFT:
                nullVariables = union(variablesProjectedByOtherChild, emptyChild.getVariables());
                return new NodeTransformationProposalImpl(DECLARE_AS_EMPTY, nullVariables);

            case RIGHT:
                nullVariables = emptyChild.getVariables().stream()
                        .filter(v -> !(variablesProjectedByOtherChild.contains(v)))
                        .collect(ImmutableCollectors.toSet());
                return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_NON_EMPTY_CHILD,
                        otherChild, nullVariables);
            default:
                throw new IllegalStateException("Unknown position: " + emptyNodePosition);
        }
    }

    @Override
    public NodeTransformationProposal reactToTrueChildRemovalProposal(IntermediateQuery query, TrueNode trueChild) {
        ArgumentPosition trueNodePosition = query.getOptionalPosition(this, trueChild)
                .orElseThrow(() -> new IllegalStateException("The deleted child of a LJ must have a position"));
        QueryNode otherChild = query.getChild(this, (trueNodePosition == LEFT) ? RIGHT : LEFT)
                .orElseThrow(() -> new IllegalStateException("The other child of a LJ is missing"));
        switch(trueNodePosition) {
            case LEFT:
                throw new UnsupportedOperationException("A TrueNode in the left position of a LeftJoin should not be removed");
            case RIGHT:
                Optional<ImmutableExpression> condition = getOptionalFilterCondition();
                if (condition.isPresent()) {
                    return new NodeTransformationProposalImpl(
                            REPLACE_BY_NEW_NODE,
                            query.getFactory().createFilterNode(condition.get()),
                            ImmutableSet.of()
                    );
                }
                return new NodeTransformationProposalImpl(REPLACE_BY_UNIQUE_NON_EMPTY_CHILD,
                        otherChild, ImmutableSet.of());
            default:
                throw new IllegalStateException("Unknown position: " + trueNodePosition);
        }
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return queryNode instanceof LeftJoinNode
                && getOptionalFilterCondition().equals(((LeftJoinNode) queryNode).getOptionalFilterCondition());
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public String toString() {
        return LEFT_JOIN_NODE_STR + getOptionalFilterString();
    }

    /**
     * TODO: explain
     *
     * TODO: move it to the NonCommutativeOperatorNodeImpl when the latter will be created.
     */
    protected boolean isFromRightBranch(QueryNode childNode, IntermediateQuery query) {
        Optional<ArgumentPosition> optionalPosition = query.getOptionalPosition(this, childNode);
        if (optionalPosition.isPresent()) {
            switch(optionalPosition.get()) {
                case LEFT:
                    return false;
                case RIGHT:
                    return true;
                default:
                    throw new RuntimeException("Unexpected position: " + optionalPosition.get());
            }
        }
        else {
            throw new RuntimeException("Inconsistent tree: no argument position after " + this);
        }
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables(IQTree leftChild, IQTree rightChild) {
        ImmutableSet<Variable> leftVariables = leftChild.getVariables();

        return Stream.concat(
                leftChild.getNullableVariables().stream(),
                // Right-specific variables
                rightChild.getVariables().stream()
                    .filter(v -> !leftVariables.contains(v)))
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public IQTree liftBinding(IQTree initialLeftChild, IQTree initialRightChild, VariableGenerator variableGenerator,
                              IQProperties currentIQProperties) {

        ImmutableSet<Variable> projectedVariables = Stream.of(initialLeftChild, initialRightChild)
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        IQTree liftedLeftChild = initialLeftChild.liftBinding(variableGenerator);
        if (liftedLeftChild.isDeclaredAsEmpty())
            return iqFactory.createEmptyNode(projectedVariables);

        // Non-final
        ChildLiftingState liftingState = liftLeftChild(liftedLeftChild, initialRightChild, getOptionalFilterCondition(),
                variableGenerator);
        boolean hasConverged = false;

        int i = 0;
        while ((!hasConverged) && (i++ < MAX_ITERATIONS)) {
            ChildLiftingState newLiftingState = liftRightChild(
                    optimizeLeftJoinCondition(liftingState, variableGenerator), variableGenerator);

            hasConverged = liftingState.equals(newLiftingState);
            liftingState = newLiftingState;
        }

        if (i >= MAX_ITERATIONS)
            throw new MinorOntopInternalBugException("LJ.liftBinding() did not converge after " + i);

        return convertResults2IQTree(projectedVariables, liftingState, currentIQProperties);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree leftChild, IQTree rightChild) {
        if (leftChild.getVariables().contains(variable)) {
            IQTree liftedLeftChild = leftChild.liftIncompatibleDefinitions(variable);
            QueryNode leftChildRoot = liftedLeftChild.getRootNode();

            if (leftChildRoot instanceof UnionNode
                    && ((UnionNode) leftChildRoot).hasAChildWithLiftableDefinition(variable, leftChild.getChildren())) {

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

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree leftChild, IQTree rightChild) {

        IQTree updatedLeftChild = leftChild.applyDescendingSubstitution(descendingSubstitution, constraint);

        Optional<ImmutableExpression> initialExpression = getOptionalFilterCondition();
        if (initialExpression.isPresent()) {
            try {
                ExpressionAndSubstitution expressionAndCondition = applyDescendingSubstitutionToExpression(
                        initialExpression.get(), descendingSubstitution, leftChild.getVariables(), rightChild.getVariables());

                Optional<ImmutableExpression> newConstraint = constraint
                        .map(c1 -> expressionAndCondition.optionalExpression
                                .flatMap(immutabilityTools::foldBooleanExpressions)
                                .orElse(c1));

                // TODO: remove the casts
                ImmutableSubstitution<? extends VariableOrGroundTerm> rightDescendingSubstitution =
                        ((ImmutableSubstitution<VariableOrGroundTerm>)(ImmutableSubstitution<?>)expressionAndCondition.substitution)
                                .composeWith2(descendingSubstitution);

                IQTree updatedRightChild = rightChild.applyDescendingSubstitution(rightDescendingSubstitution, newConstraint);

                return updatedRightChild.isDeclaredAsEmpty()
                        ? updatedLeftChild
                        : iqFactory.createBinaryNonCommutativeIQTree(
                                iqFactory.createLeftJoinNode(expressionAndCondition.optionalExpression),
                                updatedLeftChild, updatedRightChild);
            } catch (UnsatisfiableConditionException e) {
                return updatedLeftChild;
            }
        }
        else {
            IQTree updatedRightChild = rightChild.applyDescendingSubstitution(descendingSubstitution, constraint);
            if (updatedRightChild.isDeclaredAsEmpty())
                return updatedLeftChild;
            // TODO: lift it again!
            return iqFactory.createBinaryNonCommutativeIQTree(this, updatedLeftChild, updatedRightChild);
        }
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree leftChild, IQTree rightChild) {
        return Stream.of(leftChild, rightChild)
                .anyMatch(c -> c.isConstructed(variable));
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree leftChild, IQTree rightChild) {
        return propagateDownCondition(Optional.of(constraint), leftChild, rightChild);
    }

    private IQTree propagateDownCondition(Optional<ImmutableExpression> initialConstraint, IQTree leftChild, IQTree rightChild) {

        IQTree newLeftChild = initialConstraint
                .map(leftChild::propagateDownConstraint)
                .orElse(leftChild);

        ImmutableSet<Variable> leftVariables = leftChild.getVariables();

        try {
            ExpressionAndSubstitution conditionSimplificationResults =
                    simplifyCondition(getOptionalFilterCondition(), leftVariables);

            Optional<ImmutableExpression> rightConstraint = computeDownConstraint(initialConstraint,
                    conditionSimplificationResults);

            IQTree newRightChild = Optional.of(conditionSimplificationResults.substitution)
                    .filter(s -> !s.isEmpty())
                    .map(s -> rightChild.applyDescendingSubstitution(s, rightConstraint))
                    .orElseGet(() -> rightConstraint
                            .map(rightChild::propagateDownConstraint)
                            .orElse(rightChild));

            LeftJoinNode newLeftJoin = conditionSimplificationResults.optionalExpression.equals(getOptionalFilterCondition())
                    ? this
                    : conditionSimplificationResults.optionalExpression
                    .map(iqFactory::createLeftJoinNode)
                    .orElseGet(iqFactory::createLeftJoinNode);

            return iqFactory.createBinaryNonCommutativeIQTree(newLeftJoin, newLeftChild, newRightChild);

        } catch (UnsatisfiableConditionException e) {
            return newLeftChild;
        }
    }

    private ExpressionAndSubstitution applyDescendingSubstitutionToExpression(
            ImmutableExpression initialExpression,
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableSet<Variable> leftChildVariables, ImmutableSet<Variable> rightChildVariables)
            throws UnsatisfiableConditionException {

        ExpressionEvaluator.EvaluationResult results =
                createExpressionEvaluator().evaluateExpression(
                        descendingSubstitution.applyToBooleanExpression(initialExpression));

        if (results.isEffectiveFalse())
            throw new UnsatisfiableConditionException();

        return results.getOptionalExpression()
                .map(e -> convertIntoExpressionAndSubstitution(e, leftChildVariables, rightChildVariables))
                .orElseGet(() ->
                        new ExpressionAndSubstitution(Optional.empty(), descendingSubstitution.getNonFunctionalTermFragment()));
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


        ImmutableSet<ImmutableExpression> expressions = expression.flattenAND();
        ImmutableSet<ImmutableExpression> downSubstitutionExpressions = expressions.stream()
                .filter(e -> e.getFunctionSymbol().equals(EQ))
                .filter(e -> {
                    ImmutableList<? extends ImmutableTerm> arguments = e.getArguments();
                    return arguments.stream().allMatch(t -> t instanceof VariableOrGroundTerm)
                            && arguments.stream().anyMatch(rightVariables::contains);
                })
                .collect(ImmutableCollectors.toSet());

        ImmutableSubstitution<NonFunctionalTerm> downSubstitution =
                substitutionFactory.getSubstitution(
                        downSubstitutionExpressions.stream()
                            .map(ImmutableFunctionalTerm::getArguments)
                            .map(args -> (args.get(0) instanceof Variable) ? args : args.reverse())
                            // Rename right-specific variables if possible
                            .map(args -> ((args.get(0) instanceof Variable) && rightSpecificVariables.contains(args.get(1)))
                                    ? args.reverse() : args)
                            .collect(ImmutableCollectors.toMap(
                                    args -> (Variable) args.get(0),
                                    args -> (NonFunctionalTerm) args.get(1))));

        Optional<ImmutableExpression> newExpression = getImmutabilityTools().foldBooleanExpressions(
                expressions.stream()
                        .filter(e -> (!downSubstitutionExpressions.contains(e))
                                || e.getArguments().stream().anyMatch(rightSpecificVariables::contains)))
                .map(downSubstitution::applyToBooleanExpression);

        return new ExpressionAndSubstitution(newExpression, downSubstitution);
    }

    private ChildLiftingState liftLeftChild(IQTree liftedLeftChild, IQTree rightChild,
                                            Optional<ImmutableExpression> ljCondition,
                                            VariableGenerator variableGenerator) {

        if (liftedLeftChild.getRootNode() instanceof ConstructionNode) {
            ConstructionNode leftConstructionNode = (ConstructionNode) liftedLeftChild.getRootNode();
            IQTree leftGrandChild = ((UnaryIQTree) liftedLeftChild).getChild();

            try {
                return liftRegularChildBinding(leftConstructionNode, 0, leftGrandChild,
                        ImmutableList.of(liftedLeftChild,rightChild),
                        leftGrandChild.getVariables(), ljCondition, variableGenerator, this::convertIntoChildLiftingResults);
            }
            /*
             * Replaces the LJ by the left child
             */
            catch (UnsatisfiableConditionException e) {
                EmptyNode newRightChild = iqFactory.createEmptyNode(rightChild.getVariables());

                return new ChildLiftingState(liftedLeftChild, newRightChild, Optional.empty(),
                        substitutionFactory.getSubstitution());
            }
        }
        else if (liftedLeftChild.isDeclaredAsEmpty())
            return new ChildLiftingState(liftedLeftChild,
                    iqFactory.createEmptyNode(rightChild.getVariables()), Optional.empty(),
                    substitutionFactory.getSubstitution());
        else
            return new ChildLiftingState(liftedLeftChild, rightChild, ljCondition,
                    substitutionFactory.getSubstitution());
    }

    private ChildLiftingState optimizeLeftJoinCondition(ChildLiftingState state, VariableGenerator variableGenerator) {
        if (!state.ljCondition.isPresent())
            return state;

        ImmutableSet<Variable> leftVariables = state.leftChild.getVariables();
        IQTree rightChild = state.rightChild;

        try {
            ExpressionAndSubstitution simplificationResults = simplifyCondition(state.ljCondition, leftVariables);

            ImmutableSubstitution<NonFunctionalTerm> downSubstitution = selectDownSubstitution(
                    simplificationResults.substitution, rightChild.getVariables());

            if (downSubstitution.isEmpty())
                return new ChildLiftingState(state.leftChild, state.rightChild, simplificationResults.optionalExpression,
                        state.ascendingSubstitution);

            IQTree updatedRightChild = rightChild.applyDescendingSubstitution(downSubstitution,
                    simplificationResults.optionalExpression);

            Optional<RightProvenance> rightProvenance = createProvenanceElements(updatedRightChild, downSubstitution,
                    leftVariables, variableGenerator);

            IQTree newRightChild = rightProvenance
                    .flatMap(p -> p.constructionNode)
                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, updatedRightChild))
                    .orElse(updatedRightChild);

            ImmutableSubstitution<ImmutableTerm> newAscendingSubstitution = computeLiftableSubstitution(
                        downSubstitution, rightProvenance.map(p -> p.variable), leftVariables)
                    .composeWith(state.ascendingSubstitution);

            return new ChildLiftingState(state.leftChild, newRightChild, simplificationResults.optionalExpression,
                    newAscendingSubstitution);

        } catch (UnsatisfiableConditionException e) {
            return new ChildLiftingState(state.leftChild,
                    iqFactory.createEmptyNode(rightChild.getVariables()),
                    Optional.empty(),
                    state.ascendingSubstitution);
        }
    }

    /**
     * Selects the entries that can be applied to the right child.
     *
     * Useful when there is an equality between two variables defined on the right (otherwise would not converge)
     */
    private ImmutableSubstitution<NonFunctionalTerm> selectDownSubstitution(
            ImmutableSubstitution<NonFunctionalTerm> simplificationSubstitution, ImmutableSet<Variable> rightVariables) {
        ImmutableMap<Variable, NonFunctionalTerm> newMap = simplificationSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> rightVariables.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap());
        return substitutionFactory.getSubstitution(newMap);
    }

    private ChildLiftingState convertIntoChildLiftingResults(
            ImmutableList<IQTree> children, IQTree leftGrandChild, int leftChildPosition,
            Optional<ImmutableExpression> ljCondition, ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {

        if (children.size() != 2)
            throw new MinorOntopInternalBugException("Two children were expected, not " + children);

        IQTree newRightChild = children.get(1)
                .applyDescendingSubstitution(descendingSubstitution, ljCondition);

        return new ChildLiftingState(leftGrandChild, newRightChild, ljCondition, ascendingSubstitution);
    }


    private ChildLiftingState liftRightChild(ChildLiftingState state, VariableGenerator variableGenerator) {

        IQTree liftedRightChild = state.rightChild.liftBinding(variableGenerator);
        if (!(liftedRightChild.getRootNode() instanceof ConstructionNode)) {
            if (state.rightChild.equals(liftedRightChild))
                return state;

            return new ChildLiftingState(state.leftChild, liftedRightChild,
                    state.ljCondition.filter(c -> !liftedRightChild.isDeclaredAsEmpty()),
                    state.ascendingSubstitution);
        }

        ConstructionNode rightConstructionNode = (ConstructionNode) liftedRightChild.getRootNode();

        // Not supported
        if (rightConstructionNode.getOptionalModifiers().isPresent())
            return state;

        IQTree rightGrandChild = ((UnaryIQTree) liftedRightChild).getChild();

        ImmutableSubstitution<ImmutableTerm> rightSubstitution = rightConstructionNode.getSubstitution();

        return liftRightChild(state, rightGrandChild, rightSubstitution, variableGenerator);
    }

    private ChildLiftingState liftRightChild(ChildLiftingState childLiftingState, IQTree rightGrandChild,
                                             ImmutableSubstitution<ImmutableTerm> rightSubstitution,
                                             VariableGenerator variableGenerator) {

        // Empty substitution -> replace the construction node by its child
        if (rightSubstitution.isEmpty())
            return new ChildLiftingState(childLiftingState.leftChild, rightGrandChild,
                    childLiftingState.ljCondition, childLiftingState.ascendingSubstitution);

        ImmutableSet<Variable> leftVariables = childLiftingState.leftChild.getVariables();

        Optional<Map.Entry<Variable, Constant>> excludedEntry = extractExcludedEntry(rightSubstitution);

        ImmutableSubstitution<ImmutableTerm> selectedSubstitution = excludedEntry
                .map(excluded -> rightSubstitution.getImmutableMap().entrySet().stream()
                        .filter(e -> !e.equals(excluded))
                        .collect(ImmutableCollectors.toMap()))
                .map(substitutionFactory::getSubstitution)
                .orElse(rightSubstitution);

        // Empty selected substitution -> nothing to do
        if (selectedSubstitution.isEmpty())
            return childLiftingState;

        Optional<ImmutableExpression> notOptimizedLJCondition = applyRightSubstitutionToLJCondition(
                childLiftingState.ljCondition, selectedSubstitution, leftVariables);

        Optional<RightProvenance> rightProvenance = excludedEntry
                .map(e -> createProvenanceElements(e, rightGrandChild))
                .orElseGet(() -> createProvenanceElements(rightGrandChild, selectedSubstitution,
                        leftVariables, variableGenerator));

        IQTree newRightChild = rightProvenance
                .flatMap(p -> p.constructionNode)
                .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, rightGrandChild))
                .orElse(rightGrandChild);

        ImmutableSubstitution<ImmutableTerm> liftableSubstitution = computeLiftableSubstitution(
                selectedSubstitution, rightProvenance.map(e -> e.variable), leftVariables);

        ImmutableSubstitution<ImmutableTerm> newAscendingSubstitution = liftableSubstitution.composeWith(
                childLiftingState.ascendingSubstitution);

        return new ChildLiftingState(childLiftingState.leftChild, newRightChild, notOptimizedLJCondition,
                newAscendingSubstitution);
    }


    private Optional<RightProvenance> createProvenanceElements(Map.Entry<Variable, Constant> provenanceVariableDefinition,
                                                               IQTree rightTree) {
        Variable rightProvenanceVariable = provenanceVariableDefinition.getKey();

        ImmutableSet<Variable> newRightProjectedVariables =
                Stream.concat(Stream.of(rightProvenanceVariable),
                        rightTree.getVariables().stream())
                        .collect(ImmutableCollectors.toSet());

        ConstructionNode newRightConstructionNode = iqFactory.createConstructionNode(
                newRightProjectedVariables,
                substitutionFactory.getSubstitution(rightProvenanceVariable, provenanceVariableDefinition.getValue()));

        return Optional.of(new RightProvenance(rightProvenanceVariable, newRightConstructionNode));
    }

    /**
     * When at least one value does not depend on a right-specific variable
     *   (i.e. is a ground term or only depends on left variables)
     */
    private Optional<RightProvenance> createProvenanceElements(IQTree rightTree,
                                                               ImmutableSubstitution<? extends ImmutableTerm> selectedSubstitution,
                                                               ImmutableSet<Variable> leftVariables,
                                                               VariableGenerator variableGenerator) {
        if (selectedSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> !leftVariables.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .anyMatch(value -> value.getVariableStream()
                        .allMatch(leftVariables::contains)
                        || value.isGround())) {

            Optional<Variable> nonNullableRightVariable = rightTree.getVariables().stream()
                    .filter(v -> !leftVariables.contains(v))
                    .filter(v -> !rightTree.getNullableVariables().contains(v))
                    .findFirst();

            if (nonNullableRightVariable.isPresent()) {
                return Optional.of(new RightProvenance(nonNullableRightVariable.get()));
            }
            /*
             * Otherwise, creates a fresh variable and its construction node
             */
            else {
                Variable provenanceVariable = variableGenerator.generateNewVariable();

                ImmutableSet<Variable> newRightProjectedVariables =
                        Stream.concat(
                                    Stream.of(provenanceVariable),
                                    rightTree.getVariables().stream())
                                .collect(ImmutableCollectors.toSet());

                ConstructionNode newRightConstructionNode = iqFactory.createConstructionNode(
                        newRightProjectedVariables,
                        substitutionFactory.getSubstitution(provenanceVariable,
                                termFactory.getProvenanceSpecialConstant()));

                return Optional.of(new RightProvenance(provenanceVariable, newRightConstructionNode));
            }
        }
        else {
            return Optional.empty();
        }
    }


    /**
     * TODO: explain
     *
     * Right provenance variable: always there if needed
     *   (when some definitions do not depend on a right-specific variable)
     */
    private ImmutableSubstitution<ImmutableTerm> computeLiftableSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> selectedSubstitution,
            Optional<Variable> rightProvenanceVariable, ImmutableSet<Variable> leftVariables) {

        ImmutableMap<Variable, ImmutableTerm> newMap;
        if (rightProvenanceVariable.isPresent()) {
            newMap = selectedSubstitution.getImmutableMap().entrySet().stream()
                    .filter(e -> !leftVariables.contains(e.getKey()))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> transformRightSubstitutionValue(e.getValue(), leftVariables,
                                    rightProvenanceVariable.get())));
        }
        else {
            newMap = selectedSubstitution.getImmutableMap().entrySet().stream()
                    .filter(e -> !leftVariables.contains(e.getKey()))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> (ImmutableTerm) e.getValue()));
        }

        return substitutionFactory.getSubstitution(newMap);
    }

    private ImmutableTerm transformRightSubstitutionValue(ImmutableTerm value,
                                                          ImmutableSet<Variable> leftVariables,
                                                          Variable rightProvenanceVariable) {
        if (value.getVariableStream()
                .anyMatch(v -> !leftVariables.contains(v)))
            return value;

        return termFactory.getImmutableExpression(
                IF_ELSE_NULL,
                termFactory.getImmutableExpression(IS_NOT_NULL, rightProvenanceVariable),
                value);
    }


    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<ImmutableExpression> applyRightSubstitutionToLJCondition(
            Optional<ImmutableExpression> ljCondition,
            ImmutableSubstitution<ImmutableTerm> selectedSubstitution,
            ImmutableSet<Variable> leftVariables) {

        Stream<ImmutableExpression> equalitiesToInsert = selectedSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> leftVariables.contains(e.getKey()))
                .map(e -> termFactory.getImmutableExpression(EQ, e.getKey(), e.getValue()));

        return immutabilityTools.foldBooleanExpressions(
                Stream.concat(
                        ljCondition
                                .map(selectedSubstitution::applyToBooleanExpression)
                                .map(Stream::of)
                                .orElseGet(Stream::empty),
                        equalitiesToInsert));
    }

    private Optional<Map.Entry<Variable, Constant>> extractExcludedEntry(ImmutableSubstitution<ImmutableTerm> rightSubstitution) {
        Constant specialProvenanceConstant = termFactory.getProvenanceSpecialConstant();

        return rightSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> (e.getValue().equals(specialProvenanceConstant)))
                .map(e -> Maps.immutableEntry(e.getKey(), specialProvenanceConstant))
                .findFirst();
    }

    private IQTree convertResults2IQTree(ImmutableSet<Variable> projectedVariables, ChildLiftingState liftingState,
                                         IQProperties currentIQProperties) {



        AscendingSubstitutionNormalization ascendingNormalization = normalizeAscendingSubstitution(
                liftingState.getComposedAscendingSubstitution().reduceDomainToIntersectionWith(projectedVariables),
                projectedVariables);

        Optional<ConstructionNode> topConstructionNode = ascendingNormalization.generateTopConstructionNode();

        IQTree subTree = ascendingNormalization.normalizeChild(
                Optional.of(liftingState.rightChild)
                    .filter(rightChild -> !rightChild.isDeclaredAsEmpty())
                    // LJ
                    .map(rightChild -> (IQTree) iqFactory.createBinaryNonCommutativeIQTree(
                        iqFactory.createLeftJoinNode(liftingState.ljCondition),
                        liftingState.leftChild, liftingState.rightChild, currentIQProperties.declareLifted()))
                    // Left child
                    .orElse(liftingState.leftChild));

        return topConstructionNode
                .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, subTree, currentIQProperties.declareLifted()))
                .orElse(subTree);
    }



    private class ChildLiftingState {

        private final IQTree leftChild;
        private final IQTree rightChild;
        private final Optional<ImmutableExpression> ljCondition;
        private final ImmutableSubstitution<ImmutableTerm> ascendingSubstitution;

        private ChildLiftingState(IQTree leftChild, IQTree rightChild, Optional<ImmutableExpression> ljCondition,
                                  ImmutableSubstitution<ImmutableTerm> ascendingSubstitution) {
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.ljCondition = ljCondition;
            this.ascendingSubstitution = ascendingSubstitution;
        }

        /**
         * TODO: explain and find a better term
         */
        public ImmutableSubstitution<ImmutableTerm> getComposedAscendingSubstitution() {
            ImmutableSet<Variable> leftVariables = leftChild.getVariables();
            return Optional.of(rightChild)
                    .filter(IQTree::isDeclaredAsEmpty)
                    .map(c -> c.getVariables().stream()
                            .filter(v -> !leftVariables.contains(v))
                            .collect(ImmutableCollectors.toMap(
                                    v -> v,
                                    v -> termFactory.getNullConstant()
                            )))
                    .map(substitutionFactory::getSubstitution)
                    .map(s -> s.composeWith(ascendingSubstitution))
                    .orElse(ascendingSubstitution);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ChildLiftingState))
                return false;

            ChildLiftingState other = (ChildLiftingState) o;
            return leftChild.isEquivalentTo(other.leftChild)
                    && rightChild.isEquivalentTo(other.rightChild)
                    && ljCondition.equals(other.ljCondition)
                    && ascendingSubstitution.equals(other.ascendingSubstitution);
        }
    }

    /**
     * Elements that keep track that the right part contributed to the intermediate results:
     *
     * - Variable: right-specific, not nullable on the right
     * - Construction node (optional): defines the provenance variable (when the latter is not defined by an atom)
     */
    private static class RightProvenance {

        public final Variable variable;
        public final Optional<ConstructionNode> constructionNode;

        private RightProvenance(Variable provenanceVariable, ConstructionNode constructionNode) {
            this.variable = provenanceVariable;
            this.constructionNode = Optional.of(constructionNode);
        }

        private RightProvenance(Variable rightProvenanceVariable) {
            this.variable = rightProvenanceVariable;
            this.constructionNode = Optional.empty();
        }
    }

}
