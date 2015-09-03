package org.semanticweb.ontop.pivotalrepr.proposal.impl;


import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;

public class ReactToChildDeletionProposalImpl implements ReactToChildDeletionProposal {

    private final QueryNode parentNode;
    private final QueryNode deletedChild;

    public ReactToChildDeletionProposalImpl(QueryNode deletedChild, QueryNode parentNode) {
        this.parentNode = parentNode;
        this.deletedChild = deletedChild;
    }

    @Override
    public QueryNode getParentNode() {
        return parentNode;
    }

    @Override
    public QueryNode getDeletedChild() {
        return deletedChild;
    }

    @Override
    public ReactToChildDeletionResults castResults(ProposalResults results) {
        return (ReactToChildDeletionResults) results;
    }

}
