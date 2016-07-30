package it.unibz.inf.ontop.executor;


import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SimpleNodeCentricOptimizationProposal;

public interface SimpleNodeCentricInternalExecutor<N extends QueryNode, P extends SimpleNodeCentricOptimizationProposal<N>>
        extends NodeCentricInternalExecutor<N, NodeCentricOptimizationResults<N>, P> {
}
