package it.unibz.inf.ontop.iq.proposal.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.iq.proposal.GroundTermRemovalFromDataNodeProposal;

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
