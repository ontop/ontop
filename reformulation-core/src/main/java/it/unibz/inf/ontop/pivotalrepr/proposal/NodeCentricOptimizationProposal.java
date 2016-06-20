package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;

public interface NodeCentricOptimizationProposal<N extends QueryNode>
        extends QueryOptimizationProposal<NodeCentricOptimizationResults<N>> {

    N getFocusNode();
}
