package it.unibz.inf.ontop.executor;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

/**
 * TODO: explain
 */
public interface NodeCentricExecutor<
        N extends QueryNode,
        R extends NodeCentricOptimizationResults<N>,
        P extends NodeCentricOptimizationProposal<N, R>>
        extends ProposalExecutor<P, R> {

}
