package unibz.inf.ontop.pivotalrepr.proposal.impl;

import unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;


public class ProposalResultsImpl implements ProposalResults {

    private final IntermediateQuery resultingQuery;

    public ProposalResultsImpl(IntermediateQuery resultingQuery) {
        this.resultingQuery = resultingQuery;
    }

    @Override
    public IntermediateQuery getResultingQuery() {
        return resultingQuery;
    }
}
