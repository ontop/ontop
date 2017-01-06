package it.unibz.inf.ontop.executor.join;


import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.InnerJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.InnerJoinOptimizationProposal;

public interface InnerJoinExecutor extends SimpleNodeCentricInternalExecutor<InnerJoinNode, InnerJoinOptimizationProposal> {
}
