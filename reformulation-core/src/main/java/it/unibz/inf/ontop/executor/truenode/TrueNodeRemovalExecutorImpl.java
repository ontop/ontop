package it.unibz.inf.ontop.executor.truenode;

import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.impl.TrueNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.TrueNodeRemovalProposal;

import java.util.Optional;

/**
 * Created by jcorman on 06/10/16.
 */
public class TrueNodeRemovalExecutorImpl implements TrueNodeRemovalExecutor {
    @Override
    public NodeCentricOptimizationResults<TrueNode> apply(TrueNodeRemovalProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        TrueNode originalFocusNode = proposal.getFocusNode();

        return reactToTrueChildNode(query, originalFocusNode, treeComponent);
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private static NodeCentricOptimizationResults<TrueNode> reactToTrueChildNode(IntermediateQuery query, TrueNode trueNode,
                                                                        QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        QueryNode originalParentNode = query.getParent(trueNode).orElseThrow(EmptyQueryException::new);

        Optional<QueryNode> optionalOriginalNextSibling = query.getNextSibling(trueNode);

        NodeTransformationProposal transformationProposal = originalParentNode.reactToTrueChild(query, trueNode);


        QueryNode propagatingNode;
        Optional<QueryNode> optionalClosestAncestorNode;

        switch (transformationProposal.getState()) {
            case NO_LOCAL_CHANGE:
                propagatingNode = originalParentNode;
                break;
            case REPLACE_BY_UNIQUE_NON_EMPTY_CHILD:
                // Propagates the null variables from the replacing child
                propagatingNode = transformationProposal.getOptionalNewNodeOrReplacingChild().get();
                optionalClosestAncestorNode = applyReplacementProposal(originalParentNode, treeComponent, transformationProposal,
                        trueNode, true);
                break;

            case DECLARE_AS_TRUE:
                TrueNode newTrueNode = new TrueNodeImpl();
                treeComponent.replaceSubTree(originalParentNode, newTrueNode);

                /**
                 * Tail-recursive (cascade)
                 */
                return reactToTrueChildNode(query, newTrueNode, treeComponent);

            default:
                throw new RuntimeException("Unexpected state: " + transformationProposal.getState());
        }

        Optional<QueryNode> optionalNewNextSibling = optionalOriginalNextSibling
                /**
                 * In the case the next sibling has also been removed (should be exceptional)
                 */
                .filter(treeComponent::contains);

        if (optionalClosestAncestorNode.isPresent()) {
            /**
             * After removing the empty node(s), second phase: propagates the null variables
             */
            return propagateNullVariables(query, optionalClosestAncestorNode.get(), optionalNewNextSibling, treeComponent,
                    transformationProposal.getNullVariables(), propagatingNode, optionalTracker);
        }
        /**
         * Special case: the promoted child is now the root the query
         */
        else {
            return new NodeTrackingResultsImpl<>(query,
                    /**
                     * Next sibling (of the empty node or of the lastly removed ancestor)
                     */
                    optionalNewNextSibling,
                    /**
                     * First ancestor to remain (may have be updated)
                     */
                    optionalClosestAncestorNode, optionalTracker);
        }
    }
}
