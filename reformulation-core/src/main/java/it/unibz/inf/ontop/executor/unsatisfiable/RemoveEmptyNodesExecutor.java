package it.unibz.inf.ontop.executor.unsatisfiable;

import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodesProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionProposalImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
public class RemoveEmptyNodesExecutor implements InternalProposalExecutor<RemoveEmptyNodesProposal, ProposalResults> {

    private static boolean REQUIRE_USING_IN_PLACE_EXECUTOR = true;

    /**
     * TODO: explain
     */
    @Override
    public ProposalResults apply(RemoveEmptyNodesProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        for (EmptyNode emptyNode : treeComponent.getUnsatisfiableNodes()) {
            /**
             * Some unsatisfiable nodes may already have been deleted
             */
            if (treeComponent.contains(emptyNode)) {

                ReactToChildDeletionProposal reactionProposal = createReactionProposal(query, emptyNode);
                treeComponent.removeSubTree(emptyNode);

                // May update the query
                query.applyProposal(reactionProposal, REQUIRE_USING_IN_PLACE_EXECUTOR);
            }
        }

        return new ProposalResultsImpl(query);
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
