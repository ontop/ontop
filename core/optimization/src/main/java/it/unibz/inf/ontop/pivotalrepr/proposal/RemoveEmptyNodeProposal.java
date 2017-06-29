package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

import java.util.Optional;

/**
 * Removes the EmptyNode and reacts to this removal by restructuring the query.
 */
public interface RemoveEmptyNodeProposal extends NodeCentricOptimizationProposal<EmptyNode, NodeTrackingResults<EmptyNode>> {

    Optional<NodeTracker> getOptionalTracker(IntermediateQuery query);
}
