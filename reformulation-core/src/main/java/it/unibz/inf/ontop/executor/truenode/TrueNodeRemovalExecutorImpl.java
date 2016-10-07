package it.unibz.inf.ontop.executor.truenode;

import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.impl.TrueNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.TrueNodeRemovalProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

/**
 * Created by jcorman on 06/10/16.
 */
public class TrueNodeRemovalExecutorImpl implements TrueNodeRemovalExecutor {
    @Override
    public NodeCentricOptimizationResults<TrueNode> apply(TrueNodeRemovalProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        TrueNode originalFocusNode = proposal.getFocusNode();

        return reactToTrueChildNodeRemovalProposal(query, originalFocusNode, treeComponent);
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private static NodeCentricOptimizationResults<TrueNode> reactToTrueChildNodeRemovalProposal(IntermediateQuery query, TrueNode trueNode, QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        QueryNode originalParentNode = query.getParent(trueNode).orElseThrow(EmptyQueryException::new);

        Optional<QueryNode> optionalOriginalNextSibling = query.getNextSibling(trueNode);

        NodeTransformationProposal transformationProposal = originalParentNode.reactToTrueChildRemovalProposal(query, trueNode);

        switch (transformationProposal.getState()) {
            case NO_LOCAL_CHANGE:
                treeComponent.removeSubTree(trueNode);
                return new NodeCentricOptimizationResultsImpl<>(query, optionalOriginalNextSibling, Optional.of(originalParentNode));
            case REPLACE_BY_UNIQUE_NON_EMPTY_CHILD:
                treeComponent.removeSubTree(trueNode);
                treeComponent.removeOrReplaceNodeByUniqueChildren(originalParentNode);
                return new NodeCentricOptimizationResultsImpl<>(query, transformationProposal.getOptionalNewNodeOrReplacingChild());
            case DECLARE_AS_TRUE:
                TrueNode newTrueNode = new TrueNodeImpl();
                treeComponent.replaceSubTree(originalParentNode, newTrueNode);

                /**
                 * Tail-recursive (cascade)
                 */
                return reactToTrueChildNodeRemovalProposal(query, newTrueNode, treeComponent);

            default:
                throw new RuntimeException("Unexpected state: " + transformationProposal.getState());
        }
    }
}
