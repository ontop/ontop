package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeResults;


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
