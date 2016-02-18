package unibz.inf.ontop.pivotalrepr.proposal;

import unibz.inf.ontop.pivotalrepr.QueryNode;

public interface NodeCentricOptimizationProposal<T extends QueryNode>
        extends QueryOptimizationProposal<NodeCentricOptimizationResults<T>> {

    T getFocusNode();
}
