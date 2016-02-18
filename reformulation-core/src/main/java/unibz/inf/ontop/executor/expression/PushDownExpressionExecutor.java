package unibz.inf.ontop.executor.expression;

import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.pivotalrepr.*;
import unibz.inf.ontop.pivotalrepr.impl.FilterNodeImpl;
import unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import unibz.inf.ontop.model.ImmutableBooleanExpression;
import unibz.inf.ontop.model.impl.ImmutabilityTools;
import unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * TODO: explain
 */
public class PushDownExpressionExecutor implements NodeCentricInternalExecutor<JoinOrFilterNode, PushDownBooleanExpressionProposal> {

    /**
     * TODO: explain
     */
    @Override
    public NodeCentricOptimizationResults<JoinOrFilterNode> apply(PushDownBooleanExpressionProposal proposal,
                                                                  IntermediateQuery query,
                                                                  QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        JoinOrFilterNode focusNode = proposal.getFocusNode();

        for (Map.Entry<QueryNode, Collection<ImmutableBooleanExpression>> targetEntry : proposal.getTransferMap().asMap().entrySet()) {
            updateTarget(treeComponent, targetEntry.getKey(), targetEntry.getValue());
        }

        QueryNode firstChild = query.getFirstChild(focusNode)
                .orElseThrow(() -> new InvalidQueryOptimizationProposalException("The focus node has no children"));

        return updateFocusNode(treeComponent, focusNode, proposal.getExpressionsToKeep())
                .map(n -> new NodeCentricOptimizationResultsImpl<>(query, n))
                .orElseGet(() -> new NodeCentricOptimizationResultsImpl<>(query, Optional.of(firstChild)));
    }

    /**
     * Routing method
     *
     * Updates the treeComponent (side-effect)
     */
    private void updateTarget(QueryTreeComponent treeComponent, QueryNode targetNode,
                              Collection<ImmutableBooleanExpression> additionalExpressions) {
        if (targetNode instanceof DataNode) {
            updateDataNodeTarget(treeComponent, (DataNode) targetNode, additionalExpressions);
        }
        else if (targetNode instanceof JoinOrFilterNode) {
            updateJoinOrFilterNode(treeComponent, (JoinOrFilterNode)targetNode, additionalExpressions);
        }
        else {
            throw new RuntimeException("Unsupported target node: " + targetNode);
        }
    }

    private void updateDataNodeTarget(QueryTreeComponent treeComponent, DataNode targetNode,
                                      Collection<ImmutableBooleanExpression> additionalExpressions) {
        ImmutableBooleanExpression foldedExpression = ImmutabilityTools.foldBooleanExpressions(
                ImmutableList.copyOf(additionalExpressions)).get();
        FilterNode newFilterNode = new FilterNodeImpl(foldedExpression);

        treeComponent.insertParent(targetNode, newFilterNode);
    }

    private void updateJoinOrFilterNode(QueryTreeComponent treeComponent, JoinOrFilterNode targetNode,
                                        Collection<ImmutableBooleanExpression> additionalExpressions) {
        ImmutableList.Builder<ImmutableBooleanExpression> expressionBuilder = ImmutableList.builder();
        Optional<ImmutableBooleanExpression> optionalFormerExpression = targetNode.getOptionalFilterCondition();
        if (optionalFormerExpression.isPresent()) {
            expressionBuilder.add(optionalFormerExpression.get());
        }
        expressionBuilder.addAll(additionalExpressions);

        JoinOrFilterNode newNode = generateNewJoinOrFilterNode(targetNode, expressionBuilder.build()).get();
        treeComponent.replaceNode(targetNode, newNode);
    }

    private Optional<JoinOrFilterNode> updateFocusNode(QueryTreeComponent treeComponent, JoinOrFilterNode focusNode,
                                                       ImmutableList<ImmutableBooleanExpression> notTransferedExpressions)
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
                                                                          ImmutableList<ImmutableBooleanExpression> newExpressions) {
        Optional<ImmutableBooleanExpression> optionalExpression = ImmutabilityTools.foldBooleanExpressions(
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
