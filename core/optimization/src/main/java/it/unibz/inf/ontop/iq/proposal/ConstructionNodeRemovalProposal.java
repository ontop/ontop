package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

public interface ConstructionNodeRemovalProposal extends SimpleNodeCentricOptimizationProposal<ConstructionNode>{

    ImmutableSubstitution getSubstitution();

    QueryNode getChildSubtreeRoot();
}
