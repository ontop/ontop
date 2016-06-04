package it.unibz.inf.ontop.executor.unsatisfiable;

import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodesProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
public class RemoveEmptyNodesExecutor<N extends QueryNode> implements NodeCentricInternalExecutor<N, RemoveEmptyNodesProposal<N>> {

    private static boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;

    /**
     * TODO: explain
     */
    @Override
    public NodeCentricOptimizationResults<N> apply(RemoveEmptyNodesProposal<N> proposal, IntermediateQuery query,
                                                   QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        N originalFocusNode = proposal.getFocusNode();

        if (originalFocusNode instanceof EmptyNode) {
            return removeFocusEmptyNode((EmptyNode) originalFocusNode, query, treeComponent);
        }
        else {
            return removeEmptyNodesFromSubTree(originalFocusNode, query, treeComponent);
        }
    }

    /**
     * When the focus node is an EmptyNode (special case)
     */
    private NodeCentricOptimizationResults<N> removeFocusEmptyNode(EmptyNode emptyFocusNode, IntermediateQuery query,
                                                                   QueryTreeComponent treeComponent)
            throws EmptyQueryException {
        ReactToChildDeletionProposal reactionProposal = createReactionProposal(query, emptyFocusNode);
        treeComponent.removeSubTree(emptyFocusNode);

        // May update the query
        ReactToChildDeletionResults reactionResults = query.applyProposal(reactionProposal,
                REQUIRE_USING_IN_PLACE_EXECUTOR);

        return new NodeCentricOptimizationResultsImpl<>(
                query,
                reactionResults.getOptionalNextSibling(),
                Optional.of(reactionResults.getClosestRemainingAncestor()));
    }

    /**
     * Standard case
     */
    private NodeCentricOptimizationResults<N> removeEmptyNodesFromSubTree(N originalFocusNode, IntermediateQuery query,
                                                                          QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        final Optional<QueryNode> originalOptionalParent = query.getParent(originalFocusNode);
        final Optional<QueryNode> originalOptionalNextSibling = query.getNextSibling(originalFocusNode);

        // Non-final
        Optional<ReactToChildDeletionResults> optionalReactionResults = Optional.empty();


        for (EmptyNode emptyNode : treeComponent.getEmptyNodes(originalFocusNode)) {
            /**
             * Some unsatisfiable nodes may already have been deleted
             */
            if (treeComponent.contains(emptyNode)) {

                ReactToChildDeletionProposal reactionProposal = createReactionProposal(query, emptyNode);
                treeComponent.removeSubTree(emptyNode);

                // Should update the query
                optionalReactionResults = Optional.of(query.applyProposal(reactionProposal,
                        REQUIRE_USING_IN_PLACE_EXECUTOR));
            }
        }

        /**
         * *******************
         *
         * Builds the reaction results
         *
         * TODO: refactor it, based on a much richer feedback from the reaction results.
         *
         * ******************
         */

        if (query.contains(originalFocusNode)) {
            return new NodeCentricOptimizationResultsImpl<>(query, originalFocusNode);
        }

        if (originalOptionalNextSibling.isPresent()) {
            QueryNode originalNextSibling = originalOptionalNextSibling.get();
            /**
             * Next sibling still present
             */
            if (query.contains(originalNextSibling)) {
                return new NodeCentricOptimizationResultsImpl<>(query, originalOptionalNextSibling,
                        query.getParent(originalNextSibling));
            }

            // Otherwise, continue
        }

        if (originalOptionalParent.isPresent()) {

            QueryNode originalParent = originalOptionalParent.get();


            /**
             * Extremely high-probability that the focus node was already the rightmost child
             * of the parent.
             *
             * Indeed, the removal of the next sibling normally implies the transformation of the parent
             * (e.g. removing the right part of LJ, removes the LJ).
             *
             *
             */
            if (query.contains(originalParent)) {
                return new NodeCentricOptimizationResultsImpl<>(query, Optional.empty(), originalOptionalParent);
            }
            /**
             * Fail-back case: the parent has updated or removed.
             *
             * Then, the remaining parent is said to be the root
             *
             * TODO: make it more robust by looking at the previous siblings or at the children.
             */
            else {
                throw new RuntimeException("TODO: support the case where the parent has been changed and no " +
                        "trace remain a potential next sibling");
            }
        }
        /**
         * Root case: no original parent, no original sibling.
         */
        else {
            return new NodeCentricOptimizationResultsImpl<N>(query, Optional.empty(), Optional.empty());
        }
    }

    private static ReactToChildDeletionProposal createReactionProposal(IntermediateQuery query,
                                                                       EmptyNode emptyNode)
            throws EmptyQueryException {
        QueryNode parentNode = query.getParent(emptyNode)
                // It is expected that the root has only one child, so if it is unsatisfiable,
                // this query will return empty results.
                .orElseThrow(EmptyQueryException::new);

        Optional<ArgumentPosition> optionalPosition = query.getOptionalPosition(parentNode, emptyNode);

        Optional<QueryNode> optionalNextSibling = query.getNextSibling(emptyNode);


        return new ReactToChildDeletionProposalImpl(parentNode, optionalNextSibling, optionalPosition,
                emptyNode.getProjectedVariables());
    }
}
