package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.DataNode;
import org.semanticweb.ontop.pivotalrepr.proposal.GroundTermRemovalFromDataNodeProposal;

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
