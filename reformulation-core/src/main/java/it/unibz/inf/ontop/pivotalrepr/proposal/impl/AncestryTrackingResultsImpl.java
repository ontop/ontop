package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.AncestryTrackingResults;

import java.util.Optional;


public class AncestryTrackingResultsImpl<N extends QueryNode> extends NodeCentricOptimizationResultsImpl<N>
    implements AncestryTrackingResults<N> {

    public AncestryTrackingResultsImpl(IntermediateQuery query, N newNode) {
        super(query, newNode);
    }

    public AncestryTrackingResultsImpl(IntermediateQuery query, Optional<QueryNode> optionalNextSibling,
                                       Optional<QueryNode> optionalClosestAncestor) {
        super(query, optionalNextSibling, optionalClosestAncestor);
    }

    public AncestryTrackingResultsImpl(IntermediateQuery query, Optional<QueryNode> optionalReplacingChild) {
        super(query, optionalReplacingChild);
    }

    @Override
    public <M extends QueryNode> NodeCentricOptimizationResults<M> generateResultsForAncestor(M originalAncestorNode) {
        throw new RuntimeException("TODO: implement it");
    }
}
