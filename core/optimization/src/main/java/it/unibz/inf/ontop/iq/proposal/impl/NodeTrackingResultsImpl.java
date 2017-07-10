package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;
import it.unibz.inf.ontop.iq.proposal.NodeTrackingResults;

import java.util.Optional;


public class NodeTrackingResultsImpl<N extends QueryNode> extends NodeCentricOptimizationResultsImpl<N>
    implements NodeTrackingResults<N> {

    private final Optional<NodeTracker> optionalTracker;

    public NodeTrackingResultsImpl(IntermediateQuery query, N newNode, Optional<NodeTracker> optionalTracker) {
        super(query, newNode);
        this.optionalTracker = optionalTracker;
    }

    public NodeTrackingResultsImpl(IntermediateQuery query, Optional<QueryNode> optionalNextSibling,
                                   Optional<QueryNode> optionalClosestAncestor,
                                   Optional<NodeTracker> optionalTracker) {
        super(query, optionalNextSibling, optionalClosestAncestor);
        this.optionalTracker = optionalTracker;
    }

    public NodeTrackingResultsImpl(IntermediateQuery query, Optional<QueryNode> optionalReplacingChild,
                                   Optional<NodeTracker> optionalTracker) {
        super(query, optionalReplacingChild);
        this.optionalTracker = optionalTracker;
    }

    @Override
    public Optional<NodeTracker> getOptionalTracker() {
        return optionalTracker;
    }
}
