package it.unibz.inf.ontop.iq.executor.expression;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.PushDownBooleanExpressionProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * TODO: explain
 */
public class PushDownBooleanExpressionExecutorImpl implements PushDownBooleanExpressionExecutor {

    private final IntermediateQueryFactory iqFactory;
    private final ImmutabilityTools immutabilityTools;

    @Inject
    private PushDownBooleanExpressionExecutorImpl(IntermediateQueryFactory iqFactory, ImmutabilityTools immutabilityTools) {
        this.iqFactory = iqFactory;
        this.immutabilityTools = immutabilityTools;
    }

    /**
     * TODO: explain
     */
    @Override
    public NodeCentricOptimizationResults<JoinOrFilterNode> apply(PushDownBooleanExpressionProposal proposal,
                                                                  IntermediateQuery query,
                                                                  QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        JoinOrFilterNode focusNode = proposal.getFocusNode();

        for (Map.Entry<CommutativeJoinOrFilterNode, Collection<ImmutableExpression>> targetEntry : proposal.getNewDirectRecipientNodes().asMap().entrySet()) {
            updateNewDirectRecipientNode(treeComponent, targetEntry.getKey(), targetEntry.getValue());
        }

        for (Map.Entry<QueryNode, Collection<ImmutableExpression>> targetEntry :
                proposal.getIndirectRecipientNodes().asMap().entrySet()) {
            updateIndirectRecipientNode(treeComponent, targetEntry.getKey(), targetEntry.getValue());
        }

        QueryNode firstChild = query.getFirstChild(focusNode)
                .orElseThrow(() -> new InvalidQueryOptimizationProposalException("The focus node has no child"));

        return updateFocusNode(treeComponent, focusNode, proposal.getExpressionsToKeep())
                .map(n -> new NodeCentricOptimizationResultsImpl<>(query, n))
                .orElseGet(() -> new NodeCentricOptimizationResultsImpl<>(query, Optional.of(firstChild)));
    }

    private void updateIndirectRecipientNode(QueryTreeComponent treeComponent, QueryNode targetNode,
                                             Collection<ImmutableExpression> additionalExpressions) {
        ImmutableExpression foldedExpression = immutabilityTools.foldBooleanExpressions(
                ImmutableList.copyOf(additionalExpressions)).get();
        FilterNode newFilterNode = iqFactory.createFilterNode(foldedExpression);

        treeComponent.insertParent(targetNode, newFilterNode);
    }

    private void updateNewDirectRecipientNode(QueryTreeComponent treeComponent, JoinOrFilterNode targetNode,
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
                treeComponent.removeOrReplaceNodeByUniqueChild(focusNode);
            } catch (IllegalTreeUpdateException e) {
                throw new InvalidQueryOptimizationProposalException("Problem when removing a filter node: " + e.getMessage());
            }
        }
        return optionalNewFocusNode;
    }

    private Optional<JoinOrFilterNode> generateNewJoinOrFilterNode(JoinOrFilterNode formerNode,
                                                                          ImmutableList<ImmutableExpression> newExpressions) {
        Optional<ImmutableExpression> optionalExpression = immutabilityTools.foldBooleanExpressions(
                newExpressions);

        if (formerNode instanceof JoinLikeNode) {
            JoinOrFilterNode newNode = ((JoinLikeNode)formerNode).changeOptionalFilterCondition(optionalExpression);
            return Optional.of(newNode);
        }
        else if (formerNode instanceof FilterNode) {
            if (optionalExpression.isPresent()) {
                return Optional.of(iqFactory.createFilterNode(optionalExpression.get()));
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
