package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;

public class ReactToChildDeletionResultsImpl extends ProposalResultsImpl implements ReactToChildDeletionResults {

    private final QueryNode closestAncestor;
    private final Optional<QueryNode> optionalNextSibling;

    public ReactToChildDeletionResultsImpl(IntermediateQuery resultingQuery, QueryNode closestAncestor,
                                           Optional<QueryNode> optionalNextSibling) {
        super(resultingQuery);
        this.closestAncestor = closestAncestor;
        this.optionalNextSibling = optionalNextSibling;

        /**
         * Checks the arguments
         */
        if (optionalNextSibling.isPresent()) {
            Optional<QueryNode> optionalParent = resultingQuery.getParent(optionalNextSibling.get());
            if ((!optionalParent.isPresent()) || optionalParent.get() != closestAncestor) {
                throw new IllegalArgumentException("The closest ancestor must be the parent of the next sibling");
            }
        }
    }

    @Override
    public QueryNode getClosestRemainingAncestor() {
        return closestAncestor;
    }

    @Override
    public Optional<QueryNode> getOptionalNextSibling() {
        return optionalNextSibling;
    }
}
