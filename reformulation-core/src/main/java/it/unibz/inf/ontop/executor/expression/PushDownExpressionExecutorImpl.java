package it.unibz.inf.ontop.executor.expression;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.impl.ImmutabilityTools;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * TODO: explain
 */
public class PushDownExpressionExecutorImpl implements PushDownExpressionExecutor {

    /**
     * TODO: explain
     */
    @Override
    public NodeCentricOptimizationResults<JoinOrFilterNode> apply(PushDownBooleanExpressionProposal proposal,
                                                                  IntermediateQuery query,
                                                                  QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        JoinOrFilterNode focusNode = proposal.getFocusNode();

        for (Map.Entry<JoinOrFilterNode, Collection<ImmutableExpression>> targetEntry : proposal.getDirectRecipients().asMap().entrySet()) {
            updateJoinOrFilterNode(treeComponent, targetEntry.getKey(), targetEntry.getValue());
        }

        for (Map.Entry<QueryNode, Collection<ImmutableExpression>> targetEntry :
                proposal.getChildOfFilterNodesToCreate().asMap().entrySet()) {
            updateChildOfFilter(treeComponent, targetEntry.getKey(), targetEntry.getValue());
        }

        QueryNode firstChild = query.getFirstChild(focusNode)
                .orElseThrow(() -> new InvalidQueryOptimizationProposalException("The focus node has no children"));

        return updateFocusNode(treeComponent, focusNode, proposal.getExpressionsToKeep())
                .map(n -> new NodeCentricOptimizationResultsImpl<>(query, n))
                .orElseGet(() -> new NodeCentricOptimizationResultsImpl<>(query, Optional.of(firstChild)));
    }

    private void updateChildOfFilter(QueryTreeComponent treeComponent, QueryNode targetNode,
                                     Collection<ImmutableExpression> additionalExpressions) {
        ImmutableExpression foldedExpression = ImmutabilityTools.foldBooleanExpressions(
                ImmutableList.copyOf(additionalExpressions)).get();
        FilterNode newFilterNode = new FilterNodeImpl(foldedExpression);

        treeComponent.insertParent(targetNode, newFilterNode);
    }

    private void updateJoinOrFilterNode(QueryTreeComponent treeComponent, JoinOrFilterNode targetNode,
                                        Collection<ImmutableExpression> additionalExpressions) {
        ImmutableList.Builder<ImmutableExpression> expressionBuilder = ImmutableList.builder();
        Optional<ImmutableExpression> optionalFormerExpression = targetNode.getOptionalFilterCondition();
        if (optionalFormerExpression.isPresent()) {
            expressionBuilder.add(optionalFormerExpression.get());
        }
        expressionBuilder.addAll(additionalExpressions);

        JoinOrFilterNode newNode = generateNewJoinOrFilterNode(targetNode, expressionBuilder.build()).get();
        treeComponent.replaceNode(targetNode, newNode);
    }

    private Optional<JoinOrFilterNode> updateFocusNode(QueryTreeComponent treeComponent, JoinOrFilterNode focusNode,
                                                       ImmutableList<ImmutableExpression> notTransferedExpressions)
            throws InvalidQueryOptimizationProposalException {

        Optional<JoinOrFilterNode> optionalNewFocusNode = generateNewJoinOrFilterNode(focusNode, notTransferedExpressions);
        if (optionalNewFocusNode.isPresent()) {
            treeComponent.replaceNode(focusNode, optionalNewFocusNode.get());
        }
        /**
         * For useless filter (without any boolean expression)
         */
        else {
            try {
                treeComponent.removeOrReplaceNodeByUniqueChildren(focusNode);
            } catch (IllegalTreeUpdateException e) {
                throw new InvalidQueryOptimizationProposalException("Problem when removing a filter node: " + e.getMessage());
            }
        }
        return optionalNewFocusNode;
    }

    /**
     * TODO: explain
     */
    private static Optional<JoinOrFilterNode> generateNewJoinOrFilterNode(JoinOrFilterNode formerNode,
                                                                          ImmutableList<ImmutableExpression> newExpressions) {
        Optional<ImmutableExpression> optionalExpression = ImmutabilityTools.foldBooleanExpressions(
                newExpressions);

        if (formerNode instanceof JoinLikeNode) {
            JoinOrFilterNode newNode = ((JoinLikeNode)formerNode).changeOptionalFilterCondition(optionalExpression);
            return Optional.of(newNode);
        }
        else if (formerNode instanceof FilterNode) {
            if (optionalExpression.isPresent()) {
                return Optional.of((JoinOrFilterNode) new FilterNodeImpl(optionalExpression.get()));
            }
            else {
                return Optional.empty();
            }
        }
        else {
            throw new RuntimeException("Unexpected type of JoinOrFilterNode: " + formerNode);
        }
    }
}
