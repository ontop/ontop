package unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import unibz.inf.ontop.pivotalrepr.DataNode;
import unibz.inf.ontop.pivotalrepr.proposal.GroundTermRemovalFromDataNodeProposal;

public class GroundTermRemovalFromDataNodeProposalImpl implements GroundTermRemovalFromDataNodeProposal {

    private final ImmutableList<DataNode> dataNodes;

    public GroundTermRemovalFromDataNodeProposalImpl(ImmutableList<DataNode> dataNodes) {
        this.dataNodes = dataNodes;
    }

    @Override
    public ImmutableList<DataNode> getDataNodesToSimplify() {
        return dataNodes;
    }
}
