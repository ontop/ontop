package it.unibz.inf.ontop.executor.join;


import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;

public interface InnerJoinExecutor extends SimpleNodeCentricExecutor<InnerJoinNode, InnerJoinOptimizationProposal> {
}
