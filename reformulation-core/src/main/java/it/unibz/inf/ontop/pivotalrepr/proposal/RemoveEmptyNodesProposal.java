package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.EmptyNode;

/**
 * Removes the EmptyNode and reacts to this removal by restructuring the query.
 */
public interface RemoveEmptyNodesProposal extends NodeCentricOptimizationProposal<EmptyNode> {
}
