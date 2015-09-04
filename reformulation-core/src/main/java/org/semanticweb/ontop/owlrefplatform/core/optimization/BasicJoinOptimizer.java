package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

/**
 * TODO: remove this class
 */
public class BasicJoinOptimizer implements IntermediateQueryOptimizer {

    /**
     * TODO: explain
     */
    private static class OneLevelOptimizationResult {
        private final IntermediateQuery query;
        private final Optional<QueryNode> optionalNewParentNode;

        private OneLevelOptimizationResult(IntermediateQuery query, Optional<QueryNode> optionalNewParentNode) {
            this.query = query;
            this.optionalNewParentNode = optionalNewParentNode;
        }

        public IntermediateQuery getQuery() {
            return query;
        }

        public Optional<QueryNode> getOptionalNewParentNode() {
            return optionalNewParentNode;
        }
    }


    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        return optimizeChildren(query, query.getRootConstructionNode()).getQuery();
    }

    /**
     * TODO: explain
     *
     * TODO: simplify so that the update of currentQuery, currentParent and optionalChild is getting clearer.
     * Clarify when DELETE CASCADING can happen.
     *
     * Recursive
     */
    private OneLevelOptimizationResult optimizeChildren(final IntermediateQuery originalQuery,
                                                        final QueryNode originalParent) throws EmptyQueryException {

        //Non-final
        IntermediateQuery currentQuery = originalQuery;
        // Non-final
        Optional<QueryNode> optionalChild = originalQuery.getFirstChild(originalParent);

        // Non-final
        QueryNode currentParent = originalParent;

        while (optionalChild.isPresent()) {
            QueryNode child = optionalChild.get();

            /**
             * Only optimizes the JOIN nodes
             */
            if (child instanceof InnerJoinNode) {
                InnerJoinOptimizationProposal proposal = new InnerJoinOptimizationProposalImpl((InnerJoinNode) child);
                try {
                    NodeCentricOptimizationResults childResults = proposal.castResults(currentQuery.applyProposal(proposal));

                    Optional<QueryNode> optionalNewChild = childResults.getOptionalNewNode();

                    /**
                     * If the JOIN is still present (not eliminated)
                     */
                    if (optionalNewChild.isPresent()) {
                        // Recursive call on the NEW child
                        OneLevelOptimizationResult grandChildResults = optimizeChildren(childResults.getResultingQuery(), optionalNewChild.get());
                        currentQuery = grandChildResults.getQuery();

                        QueryNode newNewChild = grandChildResults.getOptionalNewParentNode().get();
                        currentParent = currentQuery.getParent(newNewChild).get();

                        // Continues with the next sibling
                        optionalChild = currentQuery.nextSibling(newNewChild);
                    }
                    /**
                     * TODO: analyze and apply the consequences of the removal of the JOIN node.
                     */
                    else {
                        currentParent = childResults.getOptionalClosestAncestor().get();
                         // Continues with the next sibling
                        optionalChild = childResults.getOptionalNextSibling();
                    }

                } catch (InvalidQueryOptimizationProposalException e) {
                    // TODO: find a better exception
                    throw new RuntimeException(e.getMessage());
                }
            }
            /**
             * Not an inner join
             */
            else {
                OneLevelOptimizationResult grandChildResults = optimizeChildren(currentQuery, child);
                currentQuery = grandChildResults.getQuery();

                // TODO: ugly!!
                QueryNode newChild = grandChildResults.getOptionalNewParentNode().get();

                Optional<QueryNode> optionalNewParent = currentQuery.getParent(newChild);
                if (!optionalNewParent.isPresent()) {
                    return new OneLevelOptimizationResult(currentQuery, Optional.<QueryNode>absent());
                }
                /**
                 * Continue looping
                 */
                else {
                    currentParent = optionalNewParent.get();
                    optionalChild = currentQuery.nextSibling(newChild);
                }
            }
        }

        return new OneLevelOptimizationResult(currentQuery, Optional.of(currentParent));
    }
}
