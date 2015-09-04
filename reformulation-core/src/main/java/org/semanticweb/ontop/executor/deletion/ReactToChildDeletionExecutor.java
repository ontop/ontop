package org.semanticweb.ontop.executor.deletion;

import com.google.common.base.Optional;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.pivotalrepr.*;
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
        return analyzeAndUpdate(query, proposal.getParentNode(), proposal.getOptionalNextSibling(), treeComponent);
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private static ReactToChildDeletionResults analyzeAndUpdate(IntermediateQuery query, QueryNode parentNode,
                                                                Optional<QueryNode> optionalNextSibling,
                                                                QueryTreeComponent treeComponent)
            throws EmptyQueryException {
        ReactToChildDeletionTransformer transformer = new ReactToChildDeletionTransformer(query);

        NodeTransformationProposal transformationProposal = parentNode.acceptNodeTransformer(transformer);

        switch (transformationProposal.getState()) {
            case NO_CHANGE:
                return new ReactToChildDeletionResultsImpl(query, parentNode, optionalNextSibling);

            case REPLACE_BY_UNIQUE_CHILD:
                return applyReplacementProposal(query, parentNode, treeComponent, transformationProposal, true);

            case REPLACE_BY_NEW_NODE:
                return applyReplacementProposal(query, parentNode, treeComponent, transformationProposal, false);

            case DELETE:
                return applyDeletionProposal(query, parentNode, treeComponent);

            default:
                throw new RuntimeException("Unexpected state: " + transformationProposal.getState());
        }
    }

    private static ReactToChildDeletionResults applyReplacementProposal(IntermediateQuery query,
                                                                        QueryNode parentNode,
                                                                        QueryTreeComponent treeComponent,
                                                                        NodeTransformationProposal transformationProposal,
                                                                        boolean isReplacedByUniqueChild) {
        Optional<QueryNode> optionalGrandParent = treeComponent.getParent(parentNode);

        if (!optionalGrandParent.isPresent()) {
            throw new RuntimeException("The root of the tree is not expected to be replaced.");
        }

        Optional<QueryNode> optionalReplacingNode = transformationProposal.getOptionalNewNode();
        if (!optionalReplacingNode.isPresent()) {
            throw new RuntimeException("Inconsistent transformation proposal: a replacing node must be given");
        }
        try {
            if (isReplacedByUniqueChild) {
                treeComponent.removeOrReplaceNodeByUniqueChildren(parentNode);
            }
            else {
                treeComponent.replaceNode(parentNode, optionalReplacingNode.get());
            }
        } catch (IllegalTreeUpdateException e) {
            throw new RuntimeException("Unexpected: " + e.getMessage());
        }

        /**
         * Next sibling: only when the parent is replaced by its unique remaining child.
         */
        Optional<QueryNode> optionalNextSibling;
        if (isReplacedByUniqueChild) {
            optionalNextSibling = optionalReplacingNode;
        }
        else {
            optionalNextSibling = Optional.absent();
        }
        return new ReactToChildDeletionResultsImpl(query, optionalGrandParent.get(), optionalNextSibling);
    }

    private static ReactToChildDeletionResults applyDeletionProposal(IntermediateQuery query, QueryNode parentNode,
                                                                     QueryTreeComponent treeComponent)
            throws EmptyQueryException {
        Optional<QueryNode> optionalGrandParent = query.getParent(parentNode);
        Optional<QueryNode> optionalNextSibling = query.nextSibling(parentNode);

        treeComponent.removeSubTree(parentNode);

        /**
         * Recursive (cascade)
         */
        if (optionalGrandParent.isPresent()) {
            return analyzeAndUpdate(query, optionalGrandParent.get(), optionalNextSibling, treeComponent);
        }
        /**
         * Arrived to the root
         */
        else {
            throw new EmptyQueryException();
        }
    }
}
