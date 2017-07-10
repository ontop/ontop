package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.QueryNode;

public interface NodeCentricOptimizationProposal<N extends QueryNode, R extends NodeCentricOptimizationResults<N>>
        extends QueryOptimizationProposal<R> {

    N getFocusNode();
}
