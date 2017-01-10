package it.unibz.inf.ontop.executor.unsatisfiable;

import it.unibz.inf.ontop.executor.NodeCentricExecutor;
import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeTrackingResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodeProposal;

public interface RemoveEmptyNodesExecutor extends NodeCentricExecutor<
        EmptyNode,
        NodeTrackingResults<EmptyNode>,
        RemoveEmptyNodeProposal> {
}
