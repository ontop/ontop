package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.pivotalrepr.CommutativeJoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 */
public interface PushDownBooleanExpressionProposal extends SimpleNodeCentricOptimizationProposal<JoinOrFilterNode> {

    /**
     * Roots of the subtrees receiving the boolean expression e being propagated down,
     * only if these roots natively support e (they must be CommutativeJoin or FilterNodes)
     */
    ImmutableMultimap<CommutativeJoinOrFilterNode, ImmutableExpression> getNewDirectRecipients();

    /**
     * Roots of the subtrees receiving the boolean expression e being propagated down,
     * only if these roots do not natively support e (they are not CommutativeJoin or FilterNodes).
     * A new parent FilterNode must be created for each of these roots,
     * in order to support e
     */
    ImmutableMultimap<QueryNode, ImmutableExpression> getChildOfFilterNodesToCreate();

    /**
     * TODO: explain
     */
    ImmutableList<ImmutableExpression> getExpressionsToKeep();
}
