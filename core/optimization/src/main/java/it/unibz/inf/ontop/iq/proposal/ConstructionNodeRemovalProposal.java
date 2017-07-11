package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;

public interface ConstructionNodeRemovalProposal extends SimpleNodeCentricOptimizationProposal<ConstructionNode>{

    ConstructionNode getReplacingNode();

    QueryNode getChildSubtreeRoot();
}
