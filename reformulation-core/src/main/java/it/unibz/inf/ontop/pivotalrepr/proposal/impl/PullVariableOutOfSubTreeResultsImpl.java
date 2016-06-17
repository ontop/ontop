package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.JoinLikeNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullVariableOutOfSubTreeResults;


public class PullVariableOutOfSubTreeResultsImpl<N extends JoinLikeNode> extends NodeCentricOptimizationResultsImpl<N>
    implements PullVariableOutOfSubTreeResults<N> {

    private final QueryNode newSubTreeRoot;

    public PullVariableOutOfSubTreeResultsImpl(IntermediateQuery query, N newFocusNode, QueryNode newSubTreeRoot) {
        super(query, newFocusNode);
        this.newSubTreeRoot = newSubTreeRoot;
    }

    @Override
    public QueryNode getNewSubTreeRoot() {
        return newSubTreeRoot;
    }
}
