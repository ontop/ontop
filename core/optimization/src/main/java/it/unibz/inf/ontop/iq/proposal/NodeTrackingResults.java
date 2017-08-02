package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 *
 */
public interface NodeTrackingResults<N extends QueryNode> extends NodeCentricOptimizationResults<N> {

    Optional<NodeTracker> getOptionalTracker();


}
