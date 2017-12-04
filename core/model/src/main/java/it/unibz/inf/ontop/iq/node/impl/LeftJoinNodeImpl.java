package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
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
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.DECLARE_AS_EMPTY;
import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.REPLACE_BY_NEW_NODE;
import static it.unibz.inf.ontop.iq.node.NodeTransformationProposedState.REPLACE_BY_UNIQUE_NON_EMPTY_CHILD;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

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
                             ExpressionEvaluator defaultExpressionEvaluator, IntermediateQueryFactory iqFactory,
                             ImmutabilityTools immutabilityTools) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools);
        this.substitutionFactory = substitutionFactory;
        this.valueNull = termFactory.getNullConstant();
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @AssistedInject
    private LeftJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                             TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                             ExpressionEvaluator defaultExpressionEvaluator, IntermediateQueryFactory iqFactory,
                             ImmutabilityTools immutabilityTools) {
        super(Optional.of(joiningCondition), nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator, immutabilityTools);
        this.substitutionFactory = substitutionFactory;
        this.valueNull = termFactory.getNullConstant();
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @AssistedInject
    private LeftJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, SubstitutionFactory substitutionFactory,
                             TermFactory termFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                             ExpressionEvaluator defaultExpressionEvaluator, IntermediateQueryFactory iqFactory,
                             ImmutabilityTools immutabilityTools) {
        super(Optional.empty(), nullabilityEvaluator, termFactory, typeFactory, datalogTools, defaultExpressionEvaluator,
                immutabilityTools);
        this.substitutionFactory = substitutionFactory;
        this.valueNull = termFactory.getNullConstant();
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public LeftJoinNode clone() {
        return new LeftJoinNodeImpl(getOptionalFilterCondition(), getNullabilityEvaluator(), substitutionFactory,
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), iqFactory, immutabilityTools);
    }

    @Override
    public LeftJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public LeftJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new LeftJoinNodeImpl(newOptionalFilterCondition, getNullabilityEvaluator(), substitutionFactory,
                termFactory, typeFactory, datalogTools, createExpressionEvaluator(), iqFactory, immutabilityTools);
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

        /**
         * If the substitution will set some right variables to be null
         *  -> remove the right part
         */
        if (rightVariables.stream()
                .filter(substitution::isDefining)
                .anyMatch(v -> substitution.get(v).equals(valueNull))) {
            return proposeToRemoveTheRightPart(query, substitution, Optional.of(rightVariables), Provenance.FROM_LEFT);
        }

        /**
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
        /**
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
}
