package org.semanticweb.ontop.executor;

import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

/**
 * TODO: explain
 *
 * InternalProposalExecutor are expected to manipulate directly a QueryTreeComponent
 *
 */
public interface InternalProposalExecutor<P extends QueryOptimizationProposal,
        R extends ProposalResults> extends ProposalExecutor<P> {

    /**
     * TODO: explain
     */
    public R apply(P proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException;

}
