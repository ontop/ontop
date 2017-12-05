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
import it.unibz.inf.ontop.substitution.VariableOrGroundTermSubstitution;
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

public class LeftJoinNodeImpl extends JoinLikeNodeImpl implements LeftJoinNode {


    private enum Provenance {
        FROM_ABOVE,
        FROM_LEFT,
        FROM_RIGHT
    }



    private static final String LEFT_JOIN_NODE_STR = "LJ";
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutabilityTools immutabilityTools;
    private final ValueConstant valueNull;

    @AssistedInject
    private LeftJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalJoinCondition,
                             TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                             ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                             IntermediateQueryFactory iqFactory) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, typeFactory, datalogTools, defaultExpressionEvaluator,
                immutabilityTools, substitutionFactory);
        this.substitutionFactory = substitutionFactory;
        this.valueNull = termFactory.getNullConstant();
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @AssistedInject
    private LeftJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                             TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                             ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                             IntermediateQueryFactory iqFactory) {
        super(Optional.of(joiningCondition), nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools, substitutionFactory);
        this.substitutionFactory = substitutionFactory;
        this.valueNull = termFactory.getNullConstant();
        this.immutabilityTools = immutabilityTools;
        this.iqFactory = iqFactory;
    }

    @AssistedInject
    private LeftJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                             ExpressionEvaluator defaultExpressionEvaluator, ImmutabilityTools immutabilityTools,
                             IntermediateQueryFactory iqFactory) {
        super(Optional.empty(), nullabilityEvaluator, termFactory, typeFactory, datalogTools, defaultExpressionEvaluator,
                immutabilityTools, substitutionFactory);
        this.substitutionFactory = substitutionFactory;
        this.valueNull = termFactory.getNullConstant();
        this.immutabilityTools = immutabilityTools;
        this.iqFactory = iqFactory;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public LeftJoinNode clone() {
        return new LeftJoinNodeImpl(getOptionalFilterCondition(), getNullabilityEvaluator(), substitutionFactory,
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), immutabilityTools, iqFactory);
    }

    @Override
    public LeftJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public LeftJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new LeftJoinNodeImpl(newOptionalFilterCondition, getNullabilityEvaluator(), substitutionFactory,
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), immutabilityTools, iqFactory);
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
    public IQTree liftBinding(IQTree initialLeftChild, IQTree initialRightChild, VariableGenerator variableGenerator) {

        ImmutableSet<Variable> projectedVariables = Stream.of(initialLeftChild, initialRightChild)
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        IQTree liftedLeftChild = initialLeftChild.liftBinding(variableGenerator);
        if (liftedLeftChild.isDeclaredAsEmpty())
            return iqFactory.createEmptyNode(projectedVariables);

        IQTree liftedRightChild = initialRightChild.liftBinding(variableGenerator);
        if (liftedRightChild.isDeclaredAsEmpty())
            return liftedLeftChild;

        ChildLiftingResults results = liftRightChild(liftLeftChild(liftedLeftChild, liftedRightChild, variableGenerator),
                variableGenerator);

        Optional<ConstructionNode> topConstructionNode = Optional.of(results.ascendingSubstitution)
                .filter(s -> !s.isEmpty())
                .map(s -> iqFactory.createConstructionNode(projectedVariables, s));

        IQTree subTree = Optional.of(results.rightChild)
                .filter(rightChild -> !rightChild.isDeclaredAsEmpty())
                // LJ
                .map(rightChild -> (IQTree) iqFactory.createBinaryNonCommutativeIQTree(
                        iqFactory.createLeftJoinNode(results.ljCondition),
                        results.leftChild, results.rightChild, true))
                // Left child
                .orElse(results.leftChild);

        return topConstructionNode
                .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, subTree, true))
                .orElse(subTree);
    }

    @Override
    public IQTree applyDescendingSubstitution(
            VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
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

                VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> rightDescendingSubstitution =
                        expressionAndCondition.substitution.composeWith2(descendingSubstitution);

                IQTree updatedRightChild = rightChild.applyDescendingSubstitution(rightDescendingSubstitution, newConstraint);

                return updatedRightChild.isDeclaredAsEmpty()
                        ? updatedLeftChild
                        : iqFactory.createBinaryNonCommutativeIQTree(
                                iqFactory.createLeftJoinNode(expressionAndCondition.optionalExpression),
                                updatedLeftChild, updatedRightChild);
            } catch (UnsatisfiableJoiningConditionException e) {
                return updatedLeftChild;
            }
        }
        else {
            IQTree updatedRightChild = rightChild.applyDescendingSubstitution(descendingSubstitution, constraint);
            if (updatedRightChild.isDeclaredAsEmpty())
                return updatedLeftChild;
            // TODO: lift it again!
            return iqFactory.createBinaryNonCommutativeIQTree(this, leftChild, rightChild);
        }
    }

    private ExpressionAndSubstitution applyDescendingSubstitutionToExpression(
            ImmutableExpression initialExpression,
            VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableSet<Variable> leftChildVariables, ImmutableSet<Variable> rightChildVariables)
            throws UnsatisfiableJoiningConditionException {

        ExpressionEvaluator.EvaluationResult results =
                createExpressionEvaluator().evaluateExpression(
                        descendingSubstitution.applyToBooleanExpression(initialExpression));

        if (results.isEffectiveFalse())
            throw new UnsatisfiableJoiningConditionException();

        return results.getOptionalExpression()
                .map(e -> convertIntoExpressionAndSubstitution(e, leftChildVariables, rightChildVariables))
                .orElseGet(() ->
                        new ExpressionAndSubstitution(Optional.empty(), descendingSubstitution));
    }

    /**
     * TODO: explain
     *
     * TODO: refactor !!!!!!!!!!
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
                            && arguments.stream().anyMatch(t -> t instanceof Variable);
                })
                .collect(ImmutableCollectors.toSet());

        VariableOrGroundTermSubstitution<VariableOrGroundTerm> downSubstitution =
                substitutionFactory.getVariableOrGroundTermSubstitution(
                        downSubstitutionExpressions.stream()
                            .map(ImmutableFunctionalTerm::getArguments)
                            .map(args -> (args.get(0) instanceof Variable) ? args : args.reverse())
                            // Rename right-specific variables if possible
                            .map(args -> rightSpecificVariables.contains(args.get(1)) ? args : args.reverse())
                            .collect(ImmutableCollectors.toMap(
                                    args -> (Variable) args.get(0),
                                    args -> (VariableOrGroundTerm) args.get(1))));

        Optional<ImmutableExpression> newExpression = getImmutabilityTools().foldBooleanExpressions(
                expressions.stream()
                        .filter(e -> (!downSubstitutionExpressions.contains(e))
                                || e.getArguments().stream().anyMatch(rightSpecificVariables::contains)))
                // TODO: do not apply this substitution!!!
                .map(downSubstitution::applyToBooleanExpression);

        return new ExpressionAndSubstitution(newExpression, downSubstitution);
    }

    private ChildLiftingResults liftLeftChild(IQTree liftedLeftChild, IQTree liftedRightChild,
                                              VariableGenerator variableGenerator) {
        if (liftedLeftChild.getRootNode() instanceof ConstructionNode) {
            ConstructionNode leftConstructionNode = (ConstructionNode) liftedLeftChild.getRootNode();
            IQTree leftGrandChild = ((UnaryIQTree) liftedLeftChild).getChild();

            try {
                return liftSelectedChildBinding(leftConstructionNode, leftGrandChild, ImmutableList.of(liftedRightChild),
                        getOptionalFilterCondition(), variableGenerator, this::convertIntoChildLiftingResults);
            }
            /*
             * Replaces the LJ by the left child
             */
            catch (UnsatisfiableJoiningConditionException e) {
                EmptyNode newRightChild = iqFactory.createEmptyNode(liftedRightChild.getVariables());

                return new ChildLiftingResults(liftedLeftChild, newRightChild, Optional.empty(),
                        substitutionFactory.getSubstitution());
            }
        }
        else
            return new ChildLiftingResults(liftedLeftChild, liftedRightChild, getOptionalFilterCondition(),
                    substitutionFactory.getSubstitution());
    }

    private ChildLiftingResults convertIntoChildLiftingResults(
            ImmutableList<IQTree> otherChildren, IQTree leftGrandChild,
            Optional<ImmutableExpression> ljCondition, ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
            VariableOrGroundTermSubstitution<? extends VariableOrGroundTerm> descendingSubstitution) {

        if (otherChildren.size() != 1)
            throw new MinorOntopInternalBugException("One other child was expected, not " + otherChildren);

        IQTree newRightChild = otherChildren.get(0)
                .applyDescendingSubstitution(descendingSubstitution, ljCondition);

        return new ChildLiftingResults(leftGrandChild, newRightChild, ljCondition, ascendingSubstitution);
    }


    private ChildLiftingResults liftRightChild(ChildLiftingResults childLiftingResults, VariableGenerator variableGenerator) {
        IQTree rightChild = childLiftingResults.rightChild;
        if (rightChild.isDeclaredAsEmpty()
                || (!(rightChild.getRootNode() instanceof ConstructionNode)))
            return childLiftingResults;

        ConstructionNode rightConstructionNode = (ConstructionNode) rightChild.getRootNode();

        // Not supported
        if (rightConstructionNode.getOptionalModifiers().isPresent())
            return childLiftingResults;

        IQTree rightGrandChild = ((UnaryIQTree) rightChild).getChild();

        ImmutableSubstitution<ImmutableTerm> rightSubstitution = rightConstructionNode.getSubstitution();

        // Empty substitution -> replace the construction node by its child
        if (rightSubstitution.isEmpty())
            return new ChildLiftingResults(childLiftingResults.leftChild, rightGrandChild,
                    childLiftingResults.ljCondition, childLiftingResults.ascendingSubstitution);

        ImmutableSet<Variable> leftVariables = childLiftingResults.leftChild.getVariables();

        Optional<Map.Entry<Variable, Constant>> excludedEntry = extractExcludedEntry(rightSubstitution);

        ImmutableSubstitution<ImmutableTerm> selectedSubstitution = excludedEntry
                .map(excluded -> rightSubstitution.getImmutableMap().entrySet().stream()
                        .filter(e -> !e.equals(excluded))
                        .collect(ImmutableCollectors.toMap()))
                .map(substitutionFactory::getSubstitution)
                .orElse(rightSubstitution);

        try {
            /*
             * TODO: do not evaluate it now (later)
             */
            Optional<ImmutableExpression> nonShrinkedLJCondition = applyRightSubstitutionToLJCondition(
                    childLiftingResults.ljCondition, selectedSubstitution, leftVariables);

            // TODO: explain
            Optional<Variable> rightProvenanceVariable;
            Optional<ConstructionNode> remainingRightConstructionNode;
            if (excludedEntry.isPresent()) {
                rightProvenanceVariable = excludedEntry.map(Map.Entry::getKey);

                ImmutableSet<Variable> newRightProjectedVariables =
                        Stream.concat(Stream.of(rightProvenanceVariable.get()),
                                rightConstructionNode.getChildVariables().stream())
                                .collect(ImmutableCollectors.toSet());

                remainingRightConstructionNode = Optional.of(iqFactory.createConstructionNode(
                        newRightProjectedVariables,
                        substitutionFactory.getSubstitution(excludedEntry
                                .map(e -> ImmutableMap.of(e.getKey(), (ImmutableTerm) e.getValue()))
                                .get())));
            }
            else if (selectedSubstitution.getImmutableMap().entrySet().stream()
                    .filter(e -> !leftVariables.contains(e.getKey()))
                    .map(Map.Entry::getValue)
                    .anyMatch(value -> value.getVariableStream()
                            .noneMatch(v -> !leftVariables.contains(v)))) {

                Variable provenanceVariable = variableGenerator.generateNewVariable();
                rightProvenanceVariable = Optional.of(provenanceVariable);

                ImmutableSet<Variable> newRightProjectedVariables =
                        Stream.concat(Stream.of(provenanceVariable),
                                rightConstructionNode.getChildVariables().stream())
                                .collect(ImmutableCollectors.toSet());

                remainingRightConstructionNode = Optional.of(iqFactory.createConstructionNode(
                        newRightProjectedVariables,
                        substitutionFactory.getSubstitution(provenanceVariable,
                                termFactory.getBooleanConstant(true))));
            }
            else {
                rightProvenanceVariable = Optional.empty();
                remainingRightConstructionNode = Optional.empty();
            }

            ImmutableSubstitution<ImmutableTerm> liftableSubstitution =
                    computeLiftableSubstitution(selectedSubstitution, rightProvenanceVariable, leftVariables);


            IQTree newRightChild = remainingRightConstructionNode
                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, rightGrandChild))
                    .orElse(rightGrandChild);

            ImmutableSubstitution<ImmutableTerm> newAscendingSubstitution = liftableSubstitution.composeWith(
                    childLiftingResults.ascendingSubstitution);

            return new ChildLiftingResults(childLiftingResults.leftChild, newRightChild, nonShrinkedLJCondition,
                    newAscendingSubstitution);

            /*
             * TODO: get rid of this
             */
        } catch (UnsatisfiableLJConditionException e) {
            return new ChildLiftingResults(childLiftingResults.leftChild,
                    iqFactory.createEmptyNode(rightChild.getVariables()), Optional.empty(),
                    childLiftingResults.ascendingSubstitution);
        }

    }

    private ImmutableSubstitution<ImmutableTerm> computeLiftableSubstitution(
            ImmutableSubstitution<ImmutableTerm> selectedSubstitution,
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
                    .collect(ImmutableCollectors.toMap());
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
            ImmutableSet<Variable> leftVariables) throws UnsatisfiableLJConditionException {

        Stream<ImmutableExpression> equalitiesToInsert = selectedSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> leftVariables.contains(e.getKey()))
                .map(e -> termFactory.getImmutableExpression(EQ, e.getKey(), e.getValue()));

        Optional<ImmutableExpression> nonOptimizedLJCondition = immutabilityTools.foldBooleanExpressions(
                Stream.concat(
                        ljCondition
                                .map(selectedSubstitution::applyToBooleanExpression)
                                .map(Stream::of)
                                .orElseGet(Stream::empty),
                        equalitiesToInsert));

        if (nonOptimizedLJCondition.isPresent()) {
            ExpressionEvaluator.EvaluationResult evaluationResults = createExpressionEvaluator()
                    .evaluateExpression(nonOptimizedLJCondition.get());
            if (evaluationResults.isEffectiveFalse())
                throw new UnsatisfiableLJConditionException();

            return evaluationResults.getOptionalExpression();
        }

        return Optional.empty();
    }

    private Optional<Map.Entry<Variable, Constant>> extractExcludedEntry(ImmutableSubstitution<ImmutableTerm> rightSubstitution) {
        return rightSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> (e.getValue() instanceof Constant))
                .map(e -> Maps.immutableEntry(e.getKey(), (Constant)e.getValue()))
                .findFirst();
    }


    private static class UnsatisfiableLJConditionException extends Exception {
    }


    private class ChildLiftingResults {

        private final IQTree leftChild;
        private final IQTree rightChild;
        private final Optional<ImmutableExpression> ljCondition;
        private final ImmutableSubstitution<ImmutableTerm> ascendingSubstitution;

        private ChildLiftingResults(IQTree leftChild, IQTree rightChild, Optional<ImmutableExpression> ljCondition,
                                   ImmutableSubstitution<ImmutableTerm> ascendingSubstitution) {
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.ljCondition = ljCondition;
            this.ascendingSubstitution = ascendingSubstitution;
        }
    }
}
