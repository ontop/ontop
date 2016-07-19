package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.AncestryTracker;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.AncestryTrackingResults;

import java.util.Optional;


public class AncestryTrackingResultsImpl<N extends QueryNode> extends NodeCentricOptimizationResultsImpl<N>
    implements AncestryTrackingResults<N> {

    private final Optional<AncestryTracker> optionalTracker;

    public AncestryTrackingResultsImpl(IntermediateQuery query, N newNode, Optional<AncestryTracker> optionalTracker) {
        super(query, newNode);
        this.optionalTracker = optionalTracker;
    }

    public AncestryTrackingResultsImpl(IntermediateQuery query, Optional<QueryNode> optionalNextSibling,
                                       Optional<QueryNode> optionalClosestAncestor,
                                       Optional<AncestryTracker> optionalTracker) {
        super(query, optionalNextSibling, optionalClosestAncestor);
        this.optionalTracker = optionalTracker;
    }

    public AncestryTrackingResultsImpl(IntermediateQuery query, Optional<QueryNode> optionalReplacingChild,
                                       Optional<AncestryTracker> optionalTracker) {
        super(query, optionalReplacingChild);
        this.optionalTracker = optionalTracker;
    }

    @Override
    public <M extends QueryNode> NodeCentricOptimizationResults<M> generateResultsForAncestor(M originalAncestorNode) {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    public Optional<AncestryTracker> getOptionalTracker() {
        return optionalTracker;
    }
}
