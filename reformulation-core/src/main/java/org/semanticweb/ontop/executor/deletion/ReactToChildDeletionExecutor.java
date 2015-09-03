package org.semanticweb.ontop.executor.deletion;

import com.google.common.base.Optional;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeException;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionResultsImpl;

/**
 * TODO: explain
 */
public class ReactToChildDeletionExecutor implements InternalProposalExecutor<ReactToChildDeletionProposal> {
    @Override
    public ReactToChildDeletionResults apply(ReactToChildDeletionProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        // May alter the query and its tree component
        QueryNode closestUnmodifiedAncestor = analyzeAndUpdate(query, proposal.getParentNode(), treeComponent);

        return new ReactToChildDeletionResultsImpl(query, closestUnmodifiedAncestor);
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private static QueryNode analyzeAndUpdate(IntermediateQuery query, QueryNode parentNode,
                                                    QueryTreeComponent treeComponent)
            throws EmptyQueryException {
        ReactToChildDeletionTransformer transformer = new ReactToChildDeletionTransformer(query);

        try {
            QueryNode newParentNode = parentNode.acceptNodeTransformer(transformer);

            return updateParentNode(query, parentNode, treeComponent, newParentNode);
        }
        catch (ReactToChildDeletionTransformer.NodeToDeleteException e) {
            return reactToDeletionException(query, parentNode, treeComponent);
        }
        catch (QueryNodeTransformationException e) {
            throw new RuntimeException("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * Returns true if the parent has been removed/replaced
     */
    private static QueryNode updateParentNode(IntermediateQuery query, QueryNode parentNode, QueryTreeComponent treeComponent,
                                         QueryNode newParentNode) {
        /**
         * Only updates if a new parent node is
         */
        if (parentNode == newParentNode) {
            return parentNode;
        }
        else if (treeComponent.contains(newParentNode)) {

                Optional<QueryNode> optionalFirstChild = query.getFirstChild(parentNode);
                if (optionalFirstChild.isPresent() && optionalFirstChild.get() == newParentNode) {
                    // Here will replace (not remove)
                    try {
                        treeComponent.removeOrReplaceNodeByUniqueChildren(parentNode);
                    }
                    /**
                     * Unexpected
                     */
                    catch (IllegalTreeUpdateException e) {
                        // TODO: should we throw another exception?
                        throw new RuntimeException("Unexpected: " + e.getMessage());
                    }
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

        /**
         * Gets the closest ancestor that has not been modified
         */
        try {
            Optional<QueryNode> optionalGrandParent = treeComponent.getParent(newParentNode);
            if (optionalGrandParent.isPresent()) {
                return optionalGrandParent.get();
            }
            else {
                // TODO: should we throw another exception?
                throw new RuntimeException("The root of the tree is not expected to be replaced.");
            }
        } catch (IllegalTreeException e) {
            // TODO: should we throw another exception?
            throw new RuntimeException("Unexpected: " + e.getMessage());
        }

    }

    /**
     * TODO: explain
     */
    private static QueryNode reactToDeletionException(IntermediateQuery query, QueryNode parentNode,
                                                            QueryTreeComponent treeComponent) throws EmptyQueryException {
        Optional<QueryNode> optionalGrandParent = query.getParent(parentNode);
        treeComponent.removeSubTree(parentNode);

        /**
         * Recursive (cascade)
         */
        if (optionalGrandParent.isPresent()) {
            return analyzeAndUpdate(query, optionalGrandParent.get(), treeComponent);
        }
        /**
         * Arrived to the root
         */
        else {
            throw new EmptyQueryException();
        }
    }
}
