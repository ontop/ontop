package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.BindingTransfer;
import org.semanticweb.ontop.pivotalrepr.proposal.ConstructionNodeUpdate;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidLocalOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionLiftProposal;

public class SubstitutionLiftProposalImpl implements SubstitutionLiftProposal {
    private final IntermediateQuery query;
    private final ImmutableList<BindingTransfer> bindingTransfers;
    private final ImmutableList<ConstructionNodeUpdate> nodeUpdates;

    public SubstitutionLiftProposalImpl(IntermediateQuery query,
                                        ImmutableList<BindingTransfer> bindingTransfers,
                                        ImmutableList<ConstructionNodeUpdate> nodeUpdates) {
        this.query = query;
        this.bindingTransfers = bindingTransfers;
        this.nodeUpdates = nodeUpdates;
    }

    @Override
    public Optional<QueryNode> apply() throws InvalidLocalOptimizationProposalException {
        query.applySubstitutionLiftProposal(this);
        return Optional.absent();
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
