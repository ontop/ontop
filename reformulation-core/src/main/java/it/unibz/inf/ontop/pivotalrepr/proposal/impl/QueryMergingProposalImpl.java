package it.unibz.inf.ontop.pivotalrepr.proposal.impl;


import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryMergingProposal;

public class QueryMergingProposalImpl implements QueryMergingProposal {
    private final IntermediateQuery subQuery;

    public QueryMergingProposalImpl(IntermediateQuery subQuery) {
        this.subQuery = subQuery;
    }

    @Override
    public IntermediateQuery getSubQuery() {
        return subQuery;
    }
}
