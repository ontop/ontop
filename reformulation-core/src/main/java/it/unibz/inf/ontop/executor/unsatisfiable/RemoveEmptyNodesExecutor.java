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
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
public class RemoveEmptyNodesExecutor implements NodeCentricInternalExecutor<EmptyNode, RemoveEmptyNodesProposal> {

    private static boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;

    /**
     * TODO: explain
     */
    @Override
    public NodeCentricOptimizationResults<EmptyNode> apply(RemoveEmptyNodesProposal proposal, IntermediateQuery query,
                                                           QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        return removeFocusEmptyNode(proposal.getFocusNode(), query, treeComponent);
    }

    /**
     * When the focus node is an EmptyNode (special case)
     */
    private NodeCentricOptimizationResults<EmptyNode> removeFocusEmptyNode(EmptyNode emptyFocusNode, IntermediateQuery query,
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
