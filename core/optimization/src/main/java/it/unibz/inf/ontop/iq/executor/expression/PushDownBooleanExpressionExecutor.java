package it.unibz.inf.ontop.iq.executor.expression;

import it.unibz.inf.ontop.iq.executor.SimpleNodeCentricExecutor;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.iq.proposal.PushDownBooleanExpressionProposal;

/**
 * TODO: explain
 */
public interface PushDownBooleanExpressionExecutor extends
        SimpleNodeCentricExecutor<JoinOrFilterNode, PushDownBooleanExpressionProposal> {
}
