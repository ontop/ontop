package org.semanticweb.ontop.executor.deletion;

import com.google.common.base.Optional;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;

/**
 * TODO: explain
 */
public class ReactToChildDeletionExecutor implements InternalProposalExecutor<ReactToChildDeletionProposal> {
    @Override
    public ProposalResults apply(ReactToChildDeletionProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        // May alter the query and its tree component
        analyzeAndUpdate(query, proposal.getParentNode(), treeComponent);

        // TODO: should we return more details?
        return new ProposalResultsImpl(query);
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private static void analyzeAndUpdate(IntermediateQuery query, QueryNode parentNode,
                                               QueryTreeComponent treeComponent) throws EmptyQueryException {
        ReactToChildDeletionTransformer transformer = new ReactToChildDeletionTransformer(query);

        try {
            QueryNode newParentNode = parentNode.acceptNodeTransformer(transformer);
            updateParentNode(query, parentNode, treeComponent, newParentNode);
        }
        catch (ReactToChildDeletionTransformer.NodeToDeleteException e) {
            Optional<QueryNode> optionalGrandParent = query.getParent(parentNode);
            treeComponent.removeSubTree(parentNode);

            /**
             * Recursive (cascade)
             */
            if (optionalGrandParent.isPresent()) {
                analyzeAndUpdate(query, optionalGrandParent.get(), treeComponent);
            }
            /**
             * Arrived to the root
             */
            else {
                throw new EmptyQueryException();
            }
        }
        catch (QueryNodeTransformationException e) {
            throw new RuntimeException("Unexpected exception: " + e.getMessage());
        }
    }

    private static void updateParentNode(IntermediateQuery query, QueryNode parentNode, QueryTreeComponent treeComponent,
                                         QueryNode newParentNode) {
        /**
         * Only updates if a new parent node is
         */
        if (parentNode != newParentNode) {
            if (treeComponent.contains(newParentNode)) {

                Optional<QueryNode> optionalFirstChild = query.getFirstChild(parentNode);
                if (optionalFirstChild.isPresent() && optionalFirstChild.get() == newParentNode) {
                    throw new RuntimeException("TODO: replace the parent by the child");
                }
                else {
                    throw new UnsupportedOperationException("Unsupported QueryNode returned " +
                            "by the ReactToChildDeletionTransformer");
                }
            }
            /**
             * New node
             */
            else {
                treeComponent.replaceNode(parentNode, newParentNode);
            }
        }
    }
}
