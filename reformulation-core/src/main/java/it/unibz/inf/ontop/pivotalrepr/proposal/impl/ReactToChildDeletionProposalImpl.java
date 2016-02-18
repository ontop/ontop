package it.unibz.inf.ontop.pivotalrepr.proposal.impl;


import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;

import java.util.Optional;

public class ReactToChildDeletionProposalImpl implements ReactToChildDeletionProposal {

    private final QueryNode parentNode;
    private final QueryNode deletedChild;
    private final Optional<QueryNode> optionalNextSibling;

    public ReactToChildDeletionProposalImpl(QueryNode deletedChild, QueryNode parentNode,
                                            Optional<QueryNode> optionalNextSibling) {
        this.parentNode = parentNode;
        this.deletedChild = deletedChild;
        this.optionalNextSibling = optionalNextSibling;
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
    public Optional<QueryNode> getOptionalNextSibling() {
        return optionalNextSibling;
    }
}
