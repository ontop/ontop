package it.unibz.inf.ontop.pivotalrepr.proposal.impl;


import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.UnionNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.UnionLiftProposal;

public class UnionLiftProposalImpl implements UnionLiftProposal {
    private final UnionNode focusNode;
    private final QueryNode targetNode;

    public UnionLiftProposalImpl(UnionNode focusNode, QueryNode targetNode) {
        this.focusNode = focusNode;
        this.targetNode = targetNode;
    }

    @Override
    public QueryNode getTargetNode() {
        return targetNode;
    }

    @Override
    public UnionNode getFocusNode() {
        return focusNode;
    }
}
