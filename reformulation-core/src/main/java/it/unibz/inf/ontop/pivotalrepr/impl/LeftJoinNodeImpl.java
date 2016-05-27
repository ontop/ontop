package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.*;
import static it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools.extractProjectedVariables;

public class LeftJoinNodeImpl extends JoinLikeNodeImpl implements LeftJoinNode {

    private static final String LEFT_JOIN_NODE_STR = "LJ";

    public LeftJoinNodeImpl(Optional<ImmutableExpression> optionalJoinCondition) {
        super(optionalJoinCondition);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public LeftJoinNode clone() {
        return new LeftJoinNodeImpl(getOptionalFilterCondition());
    }

    @Override
    public LeftJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public LeftJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return new LeftJoinNodeImpl(newOptionalFilterCondition);
    }

    @Override
    public SubstitutionResults<LeftJoinNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode descendantNode, IntermediateQuery query) {
        return  isFromRightBranch(descendantNode, query)
                ? applyAscendingSubstitutionFromRight(substitution, query)
                : applyAscendingSubstitutionFromLeft(substitution, query);
    }

    private SubstitutionResults<LeftJoinNode> applyAscendingSubstitutionFromRight(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        QueryNode leftChild = query.getChild(this, LEFT)
                .orElseThrow(() -> new IllegalStateException("No left child for the LJ"));
        ImmutableSet<Variable> leftVariables = extractProjectedVariables(query, leftChild);

        /**
         * New substitution: only concerns variables specific to the right
         */
        ImmutableMap<Variable, ImmutableTerm> newSubstitutionMap = substitution.getImmutableMap().entrySet().stream()
                .filter(e -> !leftVariables.contains(e.getKey()))
                .map(e -> (Map.Entry<Variable, ImmutableTerm>)e)
                .collect(ImmutableCollectors.toMap());
        ImmutableSubstitution<ImmutableTerm> newSubstitution = new ImmutableSubstitutionImpl<>(newSubstitutionMap);

        /**
         * Updates the joining conditions (may add new equalities)
         * and propagates the new substitution if the conditions still holds.
         *
         */
        return computeAndEvaluateNewCondition(substitution, query, leftVariables)
                .map(ev -> applyEvaluation(ev, newSubstitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(this, newSubstitution));
    }

    private SubstitutionResults<LeftJoinNode> applyAscendingSubstitutionFromLeft(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        QueryNode rightChild = query.getChild(this, RIGHT)
                .orElseThrow(() -> new IllegalStateException("No right child for the LJ"));
        ImmutableSet<Variable> rightVariables = extractProjectedVariables(query, rightChild);

        /**
         * Updates the joining conditions (may add new equalities)
         * and propagates the same substitution if the conditions still holds.
         *
         */
        return computeAndEvaluateNewCondition(substitution, query, rightVariables)
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(this, substitution));
    }


    @Override
    public SubstitutionResults<LeftJoinNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {

        return getOptionalFilterCondition()
                .map(cond -> transformBooleanExpression(query, substitution, cond))
                .map(ev -> applyEvaluation(ev, substitution))
                .orElseGet(() -> new SubstitutionResultsImpl<>(this, substitution));
    }

    private SubstitutionResults<LeftJoinNode> applyEvaluation(ExpressionEvaluator.Evaluation evaluation,
                                                              ImmutableSubstitution<? extends ImmutableTerm> substitution) {
        /**
         * Joining condition does not hold: replace the LJ by its left child.
         */
        if (evaluation.isFalse()) {
            return new SubstitutionResultsImpl<>(substitution, Optional.of(LEFT));
        }
        else {
            LeftJoinNode newNode = changeOptionalFilterCondition(evaluation.getOptionalExpression());
            return new SubstitutionResultsImpl<>(newNode, substitution);
        }
    }
    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof LeftJoinNode)
                && ((LeftJoinNode) node).getOptionalFilterCondition().equals(this.getOptionalFilterCondition());
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
    protected boolean isFromRightBranch(QueryNode descendantNode, IntermediateQuery query) {

        Optional<QueryNode> optionalCurrentNode = Optional.of(descendantNode);

        while (optionalCurrentNode.isPresent()) {
            QueryNode currentNode = optionalCurrentNode.get();
            Optional<QueryNode> optionalAncestor = query.getParent(currentNode);

            if (optionalAncestor.isPresent() && (optionalAncestor.get() == this)) {
                Optional<ArgumentPosition> optionalPosition = query.getOptionalPosition(this, currentNode);
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
            else {
                optionalCurrentNode = optionalAncestor;
            }
        }
        throw new IllegalArgumentException(descendantNode.toString() +  " is not a descendant of " + this);
    }
}
