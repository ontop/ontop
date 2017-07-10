package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;

public interface UnionLiftProposal extends SimpleNodeCentricOptimizationProposal<UnionNode> {

    /**
     * The Union has to be lift just above this target node
     */
    QueryNode getTargetNode();
}
