package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.pivotalrepr.InnerJoinNode;

/**
 * TODO: explain
 */
public interface InnerJoinOptimizationProposal extends QueryOptimizationProposal {

    /**
     * TODO: describe precisely which join node we are considering
     */
    InnerJoinNode getTopJoinNode();

    NodeCentricOptimizationResults castResults(ProposalResults results);
}
