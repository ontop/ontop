package org.semanticweb.ontop.executor.deletion;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.executor.InternalProposalExecutor;
import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;

/**
 * TODO: explain
 */
public class ReactToChildDeletionExecutor implements InternalProposalExecutor<ReactToChildDeletionProposal> {
    @Override
    public ProposalResults apply(ReactToChildDeletionProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        IntermediateQuery newQuery = analyzeAndUpdate(query, proposal.getParentNode());

        // TODO: should we return more details?
        return new ProposalResultsImpl(newQuery);
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private IntermediateQuery analyzeAndUpdate(IntermediateQuery query, QueryNode parentNode) throws EmptyQueryException {

        ImmutableList<QueryNode> children = query.getCurrentSubNodesOf(parentNode);


        /**
         * TODO: implement it seriously
         */
        throw new EmptyQueryException();
    }
}
