package it.unibz.inf.ontop.iq.proposal.impl;


import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.proposal.UnionLiftProposal;

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
