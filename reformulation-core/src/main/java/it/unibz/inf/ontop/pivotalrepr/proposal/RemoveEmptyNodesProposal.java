package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;

/**
 * Removes all the EmptyNodeNode in the sub-tree of the focus node (including the latter)
 */
public interface RemoveEmptyNodesProposal<T extends QueryNode> extends NodeCentricOptimizationProposal<T> {
}
