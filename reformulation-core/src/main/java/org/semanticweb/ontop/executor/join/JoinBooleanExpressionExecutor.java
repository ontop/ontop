package org.semanticweb.ontop.executor.join;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeException;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.InnerJoinNodeImpl;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.*;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionProposalImpl;

import static org.semanticweb.ontop.executor.join.JoinExtractionUtils.*;

/**
* TODO: explain
*/
public class JoinBooleanExpressionExecutor implements InternalProposalExecutor<InnerJoinOptimizationProposal> {

    /**
     * Standard method (InternalProposalExecutor)
     */
    @Override
    public NodeCentricOptimizationResults apply(InnerJoinOptimizationProposal proposal, IntermediateQuery query,
                                              QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        InnerJoinNode originalTopJoinNode = proposal.getTopJoinNode();

        /**
         * Will remain the sames, whatever happens
         */
        Optional<QueryNode> optionalParent = query.getParent(originalTopJoinNode);
        Optional<QueryNode> optionalNextSibling = query.nextSibling(originalTopJoinNode);

        /**
         * Optimizes
         */
        Optional<InnerJoinNode> optionalNewJoinNode = transformJoin(originalTopJoinNode, query, treeComponent);

        if (optionalNewJoinNode.isPresent()) {
            return new NodeCentricOptimizationResultsImpl(query, optionalNewJoinNode.get());
        }
        else {
            ReactToChildDeletionProposal reactionProposal = new ReactToChildDeletionProposalImpl(originalTopJoinNode,
                    optionalParent.get());

            /**
             * TODO: try to get richer feedback from the optimization (for instance, which is the next sibling).
             */
            ReactToChildDeletionResults deletionResults = reactionProposal.castResults(query.applyProposal(reactionProposal));

            QueryNode closestRemainingAncestor = deletionResults.getClosestRemainingAncestor();
            /**
             * Tries to recognize the closest remaining ancestor (can only work if an InternalExecutor has been used)
             */
            if (closestRemainingAncestor == optionalParent.get()) {
                return new NodeCentricOptimizationResultsImpl(query, optionalNextSibling, optionalParent);
            }
            /**
             * Fallback mode
             * TODO: may need to improve ReactToChildDeletionResults.
             */
            else {
                throw new RuntimeException("TODO: handle the cascade deletion of the node properly");
            }
        }
    }

    /**
     * TODO: explain
     */
    private Optional<InnerJoinNode> transformJoin(InnerJoinNode topJoinNode, IntermediateQuery query,
                                          QueryTreeComponent treeComponent) {


        ImmutableList<JoinOrFilterNode> filterOrJoinNodes = extractFilterAndInnerJoinNodes(topJoinNode, query);

        Optional<ImmutableBooleanExpression> optionalAggregatedFilterCondition;
        try {
            optionalAggregatedFilterCondition = extractFoldAndOptimizeBooleanExpressions(filterOrJoinNodes);
        }
        /**
         * The filter condition can be satisfied --> the join node and its sub-tree is thus removed from the tree.
         * Returns no join node.
         */
        catch (InsatisfiedExpressionException e) {
            treeComponent.removeSubTree(topJoinNode);
            return Optional.absent();
        }

        InnerJoinNode newJoinNode = new InnerJoinNodeImpl(optionalAggregatedFilterCondition);

        try {
            QueryNode parentNode = treeComponent.getParent(topJoinNode).get();
            Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition = treeComponent.getOptionalPosition(parentNode, topJoinNode);
            treeComponent.replaceNodesByOneNode(ImmutableList.<QueryNode>copyOf(filterOrJoinNodes), newJoinNode, parentNode, optionalPosition);

        } catch (IllegalTreeUpdateException | IllegalTreeException e) {
            throw new RuntimeException("Internal error: " + e.getMessage());
        }

        return Optional.of(newJoinNode);
    }



}
