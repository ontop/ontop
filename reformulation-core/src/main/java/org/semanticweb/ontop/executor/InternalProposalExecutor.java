package org.semanticweb.ontop.executor;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.QueryOptimizationProposal;

/**
 * TODO: explain
 *
 * InternalProposalExecutor are expected to manipulate directly a QueryTreeComponent
 *
 */
public interface InternalProposalExecutor<T extends QueryOptimizationProposal> extends ProposalExecutor<T> {

    /**
     * TODO: explain
     */
    public void apply(T proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException;

}
