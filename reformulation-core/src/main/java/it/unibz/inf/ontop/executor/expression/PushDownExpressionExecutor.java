package it.unibz.inf.ontop.executor.expression;

import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;

/**
 * TODO: explain
 */
public interface PushDownExpressionExecutor extends
        SimpleNodeCentricInternalExecutor<JoinOrFilterNode, PushDownBooleanExpressionProposal> {
}
