package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;

import java.util.Optional;

/**
 * Removes the EmptyNode and reacts to this removal by restructuring the query.
 */
public interface RemoveEmptyNodeProposal extends NodeCentricOptimizationProposal<EmptyNode, NodeTrackingResults<EmptyNode>> {

    Optional<NodeTracker> getOptionalTracker(IntermediateQuery query);
}
