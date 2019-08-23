package it.unibz.inf.ontop.iq.executor;


import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.SimpleNodeCentricOptimizationProposal;

public interface SimpleNodeCentricExecutor<N extends QueryNode, P extends SimpleNodeCentricOptimizationProposal<N>>
        extends NodeCentricExecutor<N, NodeCentricOptimizationResults<N>, P> {
}
