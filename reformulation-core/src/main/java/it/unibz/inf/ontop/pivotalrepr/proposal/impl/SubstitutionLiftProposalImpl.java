package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.proposal.BindingTransfer;
import it.unibz.inf.ontop.pivotalrepr.proposal.ConstructionNodeUpdate;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionLiftProposal;

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
