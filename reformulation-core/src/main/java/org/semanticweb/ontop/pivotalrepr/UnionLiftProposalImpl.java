package org.semanticweb.ontop.pivotalrepr;

public class UnionLiftProposalImpl implements UnionLiftProposal {

    private final UnionNode unionNode;

    public UnionLiftProposalImpl(UnionNode unionNode) {
        this.unionNode = unionNode;
    }

    @Override
    public UnionNode getUnionNode() {
        return unionNode;
    }

}
