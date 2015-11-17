package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;

import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.*;
import static org.semanticweb.ontop.owlrefplatform.core.optimization.QueryNodeNavigationTools.getDepthFirstNextNode;

/**
 * TODO: explain
 */
public class PushDownBooleanExpressionOptimizer implements IntermediateQueryOptimizer {


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
        // TODO: implement it
        return Optional.absent();
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
