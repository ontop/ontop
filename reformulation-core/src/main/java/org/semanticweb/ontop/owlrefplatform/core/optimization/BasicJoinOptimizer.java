package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.InnerJoinOptimizationProposalImpl;

/**
 * TODO: remove this class
 */
public class BasicJoinOptimizer implements IntermediateQueryOptimizer {
    @Override
    public IntermediateQuery optimize(IntermediateQuery query) {
        return optimizeChildren(query, query.getRootConstructionNode());
    }

    /**
     * TODO: explain
     *
     * TODO: make it more robust!!!!
     *
     * Recursive
     */
    private IntermediateQuery optimizeChildren(final IntermediateQuery query, QueryNode queryNode) {

        //Non-final
        IntermediateQuery currentQuery = query;


        ImmutableList<QueryNode> children = query.getCurrentSubNodesOf(queryNode);
        /**
         * TODO: this is weak!!! It assumes that the optimization will be applied by an InternalOptimizationExecutor!!
         */
        for (int i=0; i < children.size() ; i++) {
            QueryNode child = children.get(i);

            /**
             * Only optimizes the JOIN nodes
             */
            if (child instanceof InnerJoinNode) {
                // TODO: construct it!
                InnerJoinOptimizationProposal proposal = new InnerJoinOptimizationProposalImpl((InnerJoinNode) child);
                try {
                    currentQuery = currentQuery.applyProposal(proposal).getResultingQuery();
                } catch (InvalidQueryOptimizationProposalException e) {
                    // TODO: find a better exception
                    throw new RuntimeException(e.getMessage());
                }
            }
            // Recursive call on the NEW child
            currentQuery = optimizeChildren(currentQuery, children.get(i));
        }

        return currentQuery;
    }
}
