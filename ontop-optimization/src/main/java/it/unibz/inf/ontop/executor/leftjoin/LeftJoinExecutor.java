package it.unibz.inf.ontop.executor.leftjoin;


import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.LeftJoinNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.LeftJoinOptimizationProposal;

public interface LeftJoinExecutor extends SimpleNodeCentricInternalExecutor<LeftJoinNode, LeftJoinOptimizationProposal> {
}
