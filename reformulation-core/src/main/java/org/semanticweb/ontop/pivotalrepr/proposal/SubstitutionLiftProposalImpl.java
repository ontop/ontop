package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.ConstructionNodeUpdate;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidLocalOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionLiftProposal;

public class SubstitutionLiftProposalImpl implements SubstitutionLiftProposal {
    private final ConstructionNodeUpdate topNodeUpdate;
    private final ImmutableList<ConstructionNodeUpdate> bottomNodeUpdates;
    private final IntermediateQuery query;

    public SubstitutionLiftProposalImpl(IntermediateQuery query, ConstructionNodeUpdate topNodeUpdate,
                                        ImmutableList<ConstructionNodeUpdate> bottomNodeUpdates) {
        this.query = query;
        this.topNodeUpdate = topNodeUpdate;
        this.bottomNodeUpdates = bottomNodeUpdates;
    }

    @Override
    public ConstructionNodeUpdate getTopNodeUpdate() {
        return topNodeUpdate;
    }

    @Override
    public ImmutableList<ConstructionNodeUpdate> getBottomNodeUpdates() {
        return bottomNodeUpdates;
    }

    @Override
    public Optional<QueryNode> apply() throws InvalidLocalOptimizationProposalException {
        query.applySubstitutionLiftProposal(this);
        return Optional.absent();
    }
}
