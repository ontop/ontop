package it.unibz.inf.ontop.executor;

import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

/**
 * TODO: explain
 */
public interface NodeCentricInternalExecutor<N extends QueryNode, P extends NodeCentricOptimizationProposal<N>>
        extends InternalProposalExecutor<P, NodeCentricOptimizationResults<N>> {

}
