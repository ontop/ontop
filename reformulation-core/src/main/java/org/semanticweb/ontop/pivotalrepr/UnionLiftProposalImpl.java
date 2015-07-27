package org.semanticweb.ontop.pivotalrepr;

public class UnionLiftProposalImpl implements UnionLiftProposal {

    private final QueryNode targetNode;
    private final UnionNode unionNode;

    public UnionLiftProposalImpl(UnionNode unionNode, QueryNode targetNode) {
        this.unionNode = unionNode;
        this.targetNode = targetNode;
    }

    @Override
    public UnionNode getUnionNode() {
        return unionNode;
    }

    @Override
    public QueryNode getTargetQueryNode() {
        return targetNode;
    }
}
