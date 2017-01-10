package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;

public interface NodeCentricOptimizationProposal<N extends QueryNode, R extends NodeCentricOptimizationResults<N>>
        extends QueryOptimizationProposal<R> {

    N getFocusNode();
}
