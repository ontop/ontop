package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeTracker;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeTrackingResults;

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
    public <M extends QueryNode> NodeCentricOptimizationResults<M> generateResultsForAncestor(M originalAncestorNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public Optional<NodeTracker> getOptionalTracker() {
        return optionalTracker;
    }
}
