package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 *
 */
public interface NodeTrackingResults<N extends QueryNode> extends NodeCentricOptimizationResults<N> {

    Optional<NodeTracker> getOptionalTracker();


}
