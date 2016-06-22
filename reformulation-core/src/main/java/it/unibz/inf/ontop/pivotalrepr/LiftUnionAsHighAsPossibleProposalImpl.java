package it.unibz.inf.ontop.pivotalrepr;

public class LiftUnionAsHighAsPossibleProposalImpl implements LiftUnionAsHighAsPossibleProposal {

    private final UnionNode unionNode;

    public LiftUnionAsHighAsPossibleProposalImpl(UnionNode unionNode) {
        this.unionNode = unionNode;
    }

    @Override
    public UnionNode getUnionNode() {
        return unionNode;
    }

}
