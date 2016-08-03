package it.unibz.inf.ontop.executor.unsatisfiable;

import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeTrackingResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodeProposal;

public interface RemoveEmptyNodesExecutor extends NodeCentricInternalExecutor<
        EmptyNode,
        NodeTrackingResults<EmptyNode>,
        RemoveEmptyNodeProposal> {
}
