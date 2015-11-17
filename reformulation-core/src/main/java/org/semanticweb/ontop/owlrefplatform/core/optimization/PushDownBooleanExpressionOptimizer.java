package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;

import java.util.Iterator;

import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.*;
import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;

/**
 * TODO: explain
 */
public class PushDownBooleanExpressionOptimizer implements IntermediateQueryOptimizer {

    /**
     * TODO: explain
     */
    private static class NotSupportedCaseException extends Exception {
    }


    @Override
    public IntermediateQuery optimize(final IntermediateQuery initialQuery) throws EmptyQueryException {
        try {
            return pushDownExpressions(initialQuery);
        } catch (InvalidQueryOptimizationProposalException e) {
            throw new RuntimeException("TODO: unexcepted exception: " + e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    private IntermediateQuery pushDownExpressions(final IntermediateQuery initialQuery)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        // Non-final
        Optional<QueryNode> optionalCurrentNode = initialQuery.getFirstChild(initialQuery.getRootConstructionNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalCurrentNode.isPresent()) {
            final QueryNode currentNode = optionalCurrentNode.get();

            /**
             * InnerJoinNode, LeftJoinNode or FilterNode
             */
            if (currentNode instanceof JoinOrFilterNode) {
                NextNodeAndQuery nextNodeAndQuery = optimizeJoinOrFilter(currentQuery, (JoinOrFilterNode) currentNode);
                optionalCurrentNode = nextNodeAndQuery.getOptionalNextNode();
                currentQuery = nextNodeAndQuery.getNextQuery();
            }
            else {
                optionalCurrentNode = getDepthFirstNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }


    /**
     * TODO: explain
     */
    private NextNodeAndQuery optimizeJoinOrFilter(IntermediateQuery currentQuery, JoinOrFilterNode currentNode)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        Optional<PushDownBooleanExpressionProposal<? extends JoinOrFilterNode>> optionalProposal = makeProposal(
                currentQuery, currentNode);

        if (optionalProposal.isPresent()) {
            PushDownBooleanExpressionProposal<? extends JoinOrFilterNode> proposal = optionalProposal.get();

            // Applies the proposal and casts the results
            NodeCentricOptimizationResults<? extends JoinOrFilterNode> results = proposal.castResults(
                    currentQuery.applyProposal(proposal));

            return getNextNodeAndQuery(results);
        }
        else {
            return new NextNodeAndQuery(getDepthFirstNextNode(currentQuery, currentNode), currentQuery);
        }
    }

    /**
     * Routing method
     */
    private Optional<PushDownBooleanExpressionProposal<? extends JoinOrFilterNode>> makeProposal(
            IntermediateQuery currentQuery, JoinOrFilterNode currentNode) {
        if (currentNode instanceof InnerJoinNode) {
            return makeProposalForInnerJoin(currentQuery, (InnerJoinNode) currentNode);
        }
        else if (currentNode instanceof FilterNode) {
            return makeProposalForFilter(currentQuery, (FilterNode) currentNode);
        }
        /**
         * Left-join is not yet supported
         */
        else {
            return Optional.absent();
        }
    }

    /**
     * TODO: explain
     */
    private Optional<PushDownBooleanExpressionProposal<? extends JoinOrFilterNode>> makeProposalForInnerJoin(
            IntermediateQuery currentQuery, InnerJoinNode currentNode) {

        Optional<ImmutableBooleanExpression> optionalNestedExpression = currentNode.getOptionalFilterCondition();
        if (!optionalNestedExpression.isPresent()) {
            return Optional.absent();
        }

        ImmutableSet<ImmutableBooleanExpression> booleanExpressions = optionalNestedExpression.get().flatten();

        ImmutableList<QueryNode> potentialTargetNodes = findCandidateTargetNodes(currentQuery, currentNode);

        // TODO: continue
        throw new RuntimeException("TODO: continue");

    }

    /**
     * TODO: find a better name
     *
     * TODO: explain
     */
    private ImmutableList<QueryNode> findCandidateTargetNodes( IntermediateQuery currentQuery,
                                                               InnerJoinNode currentNode) {
        try {
            ImmutableList.Builder<QueryNode> candidateListBuilder = ImmutableList.builder();

            for (QueryNode childNode : currentQuery.getChildren(currentNode)) {
                candidateListBuilder.addAll(findCandidatesInSubTree(currentQuery, childNode, false));
            }
            return candidateListBuilder.build();
        } catch (NotSupportedCaseException e) {
            return ImmutableList.of();
        }
    }

    /**
     * TODO: explain and clean
     */
    private ImmutableList<QueryNode> findCandidatesInSubTree(IntermediateQuery currentQuery,
                                                             QueryNode node,
                                                             boolean behindADelimiterNode)
            throws NotSupportedCaseException {
        if (node instanceof DataNode) {
            if (behindADelimiterNode) {
                return ImmutableList.of(node);
            }
            else {
                throw new NotSupportedCaseException();
            }
        }

        boolean childBehindDelimiter = behindADelimiterNode;

        if ((node instanceof JoinOrFilterNode) && behindADelimiterNode) {
            return ImmutableList.of(node);
        }
        /**
         * Construction nodes or extensions
         */
        else if (node instanceof SubTreeDelimiterNode) {
            childBehindDelimiter = true;
        }

        ImmutableList.Builder<QueryNode> candidateListBuilder = ImmutableList.builder();
        for (QueryNode child : currentQuery.getChildren(node)) {
            // Recursive call
            candidateListBuilder.addAll(findCandidatesInSubTree(currentQuery, child, childBehindDelimiter));
        }
        return candidateListBuilder.build();
    }

    /**
     * TODO: implement
     *
     * Handle get replacing first child?
     *
     */
    private Optional<PushDownBooleanExpressionProposal<? extends JoinOrFilterNode>> makeProposalForFilter(
            IntermediateQuery currentQuery, FilterNode currentNode) {
        // TODO: implement it
        return Optional.absent();
    }

}
