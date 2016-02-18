package unibz.inf.ontop.executor;

import unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

/**
 * TODO: explain
 *
 * InternalProposalExecutor are expected to manipulate directly a QueryTreeComponent
 *
 */
public interface InternalProposalExecutor<P extends QueryOptimizationProposal<R>,
        R extends ProposalResults> extends ProposalExecutor<P, R> {

    /**
     * TODO: explain
     */
    public R apply(P proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException;

}
