package unibz.inf.ontop.executor.deletion;

import unibz.inf.ontop.executor.InternalProposalExecutor;
import unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import unibz.inf.ontop.pivotalrepr.NodeTransformationProposal;
import unibz.inf.ontop.pivotalrepr.QueryNode;
import unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;
import unibz.inf.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionResultsImpl;

import java.util.Optional;

/**
 * TODO: explain
 */
public class ReactToChildDeletionExecutor implements InternalProposalExecutor<ReactToChildDeletionProposal,
        ReactToChildDeletionResults> {
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
                                                                java.util.Optional<QueryNode> optionalNextSibling,
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
            throw new InvalidQueryOptimizationProposalException("The root of the tree is not expected to be replaced.");
        }

        Optional<QueryNode> optionalReplacingNode = transformationProposal.getOptionalNewNode();
        if (!optionalReplacingNode.isPresent()) {
            throw new InvalidQueryOptimizationProposalException("Inconsistent transformation proposal: a replacing node must be given");
        }
        if (isReplacedByUniqueChild) {
            treeComponent.removeOrReplaceNodeByUniqueChildren(parentNode);
        }
        else {
            treeComponent.replaceNode(parentNode, optionalReplacingNode.get());
        }

        /**
         * Next sibling: only when the parent is replaced by its unique remaining child.
         */
        Optional<QueryNode> optionalNextSibling;
        if (isReplacedByUniqueChild) {
            optionalNextSibling = optionalReplacingNode;
        }
        else {
            optionalNextSibling = Optional.empty();
        }
        return new ReactToChildDeletionResultsImpl(query, optionalGrandParent.get(), optionalNextSibling);
    }

    private static ReactToChildDeletionResults applyDeletionProposal(IntermediateQuery query, QueryNode parentNode,
                                                                     QueryTreeComponent treeComponent)
            throws EmptyQueryException {
        Optional<QueryNode> optionalGrandParent = query.getParent(parentNode);
        Optional<QueryNode> optionalNextSibling = query.getNextSibling(parentNode);

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
