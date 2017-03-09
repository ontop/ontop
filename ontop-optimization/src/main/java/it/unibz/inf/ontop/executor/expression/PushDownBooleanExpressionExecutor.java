package it.unibz.inf.ontop.executor.expression;

import it.unibz.inf.ontop.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;

/**
 * TODO: explain
 */
public interface PushDownBooleanExpressionExecutor extends
        SimpleNodeCentricExecutor<JoinOrFilterNode, PushDownBooleanExpressionProposal> {
}
