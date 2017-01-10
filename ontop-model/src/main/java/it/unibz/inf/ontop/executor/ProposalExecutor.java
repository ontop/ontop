package it.unibz.inf.ontop.executor;

import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProposalResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

/**
 * TODO: explain
 *
 * ProposalExecutor are expected to manipulate directly a QueryTreeComponent
 *
 */
public interface ProposalExecutor<P extends QueryOptimizationProposal<R>, R extends ProposalResults>{

    /**
     * TODO: explain
     */
    R apply(P proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException;
}
