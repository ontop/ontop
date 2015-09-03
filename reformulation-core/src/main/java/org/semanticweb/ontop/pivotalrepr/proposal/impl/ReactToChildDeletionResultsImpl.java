package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;

public class ReactToChildDeletionResultsImpl extends ProposalResultsImpl implements ReactToChildDeletionResults {

    private final QueryNode closestAncestor;

    public ReactToChildDeletionResultsImpl(IntermediateQuery resultingQuery, QueryNode closestAncestor) {
        super(resultingQuery);
        this.closestAncestor = closestAncestor;
    }

    @Override
    public QueryNode getClosestRemainingAncestor() {
        return closestAncestor;
    }

}
