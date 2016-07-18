package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.EmptyNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodeResults;

import java.util.Optional;


public class RemoveEmptyNodeResultsImpl  extends NodeCentricOptimizationResultsImpl<EmptyNode>
    implements RemoveEmptyNodeResults {
    public RemoveEmptyNodeResultsImpl(IntermediateQuery query, EmptyNode newNode) {
        super(query, newNode);
    }

    public RemoveEmptyNodeResultsImpl(IntermediateQuery query, Optional<QueryNode> optionalNextSibling,
                                      Optional<QueryNode> optionalClosestAncestor) {
        super(query, optionalNextSibling, optionalClosestAncestor);
    }

    public RemoveEmptyNodeResultsImpl(IntermediateQuery query, Optional<QueryNode> optionalReplacingChild) {
        super(query, optionalReplacingChild);
    }
}
