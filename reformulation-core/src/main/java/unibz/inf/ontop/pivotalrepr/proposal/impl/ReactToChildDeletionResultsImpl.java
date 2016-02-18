package unibz.inf.ontop.pivotalrepr.proposal.impl;

import unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import unibz.inf.ontop.pivotalrepr.QueryNode;
import unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;

import java.util.Optional;

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
