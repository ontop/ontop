package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.proposal.BindingTransfer;
import org.semanticweb.ontop.pivotalrepr.proposal.ConstructionNodeUpdate;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionLiftProposal;

public class SubstitutionLiftProposalImpl implements SubstitutionLiftProposal {
    private final ImmutableList<BindingTransfer> bindingTransfers;
    private final ImmutableList<ConstructionNodeUpdate> nodeUpdates;

    public SubstitutionLiftProposalImpl(ImmutableList<BindingTransfer> bindingTransfers,
                                        ImmutableList<ConstructionNodeUpdate> nodeUpdates) {
        this.bindingTransfers = bindingTransfers;
        this.nodeUpdates = nodeUpdates;
    }

    @Override
    public ImmutableList<BindingTransfer> getBindingTransfers() {
        return bindingTransfers;
    }

    @Override
    public ImmutableList<ConstructionNodeUpdate> getNodeUpdates() {
        return nodeUpdates;
    }
}
