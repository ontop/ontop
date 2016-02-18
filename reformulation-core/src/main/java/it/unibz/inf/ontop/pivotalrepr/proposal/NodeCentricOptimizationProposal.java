package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;

public interface NodeCentricOptimizationProposal<T extends QueryNode>
        extends QueryOptimizationProposal<NodeCentricOptimizationResults<T>> {

    T getFocusNode();
}
