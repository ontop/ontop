package org.semanticweb.ontop.executor;

import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

/**
 * TODO: explain
 */
public interface NodeCentricInternalExecutor<N extends QueryNode, P extends NodeCentricOptimizationProposal<N>>
        extends InternalProposalExecutor<P, NodeCentricOptimizationResults<N>> {

}
