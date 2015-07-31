package org.semanticweb.ontop.owlrefplatform.core.optimization;

import com.google.common.base.Optional;
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
    private IntermediateQuery optimizeChildren(final IntermediateQuery originalQuery, QueryNode queryNode) {

        //Non-final
        IntermediateQuery currentQuery = originalQuery;


        // Non-final
        Optional<QueryNode> optionalChild = originalQuery.getFirstChild(queryNode);
        while (optionalChild.isPresent()) {
            QueryNode child = optionalChild.get();

            /**
             * Only optimizes the JOIN nodes
             */
            if (child instanceof InnerJoinNode) {
                // TODO: construct it!
                InnerJoinOptimizationProposal proposal = new InnerJoinOptimizationProposalImpl((InnerJoinNode) child);
                try {
                    NodeCentricOptimizationResults results = proposal.castResults(currentQuery.applyProposal(proposal));

                    Optional<QueryNode> optionalNewChild = results.getOptionalNewNode();

                    if (optionalNewChild.isPresent()) {
                        // Recursive call on the NEW child
                        currentQuery = optimizeChildren(results.getResultingQuery(), optionalNewChild.get());
                    }

                    /**
                     * Continues with the next sibling
                     */
                    optionalChild = results.getOptionalNextSibling();

                } catch (InvalidQueryOptimizationProposalException e) {
                    // TODO: find a better exception
                    throw new RuntimeException(e.getMessage());
                }
            }
            /**
             * No optimization
             */
            else {
                optionalChild = currentQuery.nextSibling(child);
            }
        }

        return currentQuery;
    }
}
