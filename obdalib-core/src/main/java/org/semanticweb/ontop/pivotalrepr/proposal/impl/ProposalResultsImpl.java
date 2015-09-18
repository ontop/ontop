package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;


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
