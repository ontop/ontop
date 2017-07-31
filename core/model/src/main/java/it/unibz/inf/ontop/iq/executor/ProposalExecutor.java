package it.unibz.inf.ontop.iq.executor;

import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.ProposalResults;
import it.unibz.inf.ontop.iq.proposal.QueryOptimizationProposal;

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
