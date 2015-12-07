package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.pivotalrepr.QueryNode;

public interface NodeCentricOptimizationProposal<T extends QueryNode>
        extends QueryOptimizationProposal<NodeCentricOptimizationResults<T>> {

    /**
     * TODO: remove this method
     */
    @Deprecated
    NodeCentricOptimizationResults<T> castResults(ProposalResults results);

    T getFocusNode();
}
