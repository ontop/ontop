package it.unibz.inf.ontop.executor.expression;

import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.CommutativeJoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushUpBooleanExpressionProposal;

public interface PushUpBooleanExpressionExecutor extends NodeCentricInternalExecutor<CommutativeJoinOrFilterNode,
        NodeCentricOptimizationResults<CommutativeJoinOrFilterNode>, PushUpBooleanExpressionProposal> {

}
