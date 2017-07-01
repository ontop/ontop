package it.unibz.inf.ontop.iq.proposal.impl;


import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.proposal.QueryMergingProposal;

import java.util.Optional;

public class QueryMergingProposalImpl implements QueryMergingProposal {
    private final Optional<IntermediateQuery> subQuery;
    private final IntensionalDataNode intensionalNode;

    public QueryMergingProposalImpl(IntensionalDataNode intensionalNode, Optional<IntermediateQuery> optionalSubQuery) {
        this.subQuery = optionalSubQuery;
        this.intensionalNode = intensionalNode;
    }

    @Override
    public IntensionalDataNode getIntensionalNode() {
        return intensionalNode;
    }

    @Override
    public Optional<IntermediateQuery> getSubQuery() {
        return subQuery;
    }
}
