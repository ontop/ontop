package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 */
public interface PushDownBooleanExpressionProposal extends SimpleNodeCentricOptimizationProposal<JoinOrFilterNode> {

    /**
     * TODO: explain
     *
     * Nodes that can directly receive these additional conditions
     */
    ImmutableMultimap<JoinOrFilterNode, ImmutableExpression> getDirectRecipients();

    /**
     * TODO: find a better name
     *
     * These QueryNode require new FilterNodes to be inserted as their parents.
     * The filter nodes will receive the corresponding conditions.
     */
    ImmutableMultimap<QueryNode, ImmutableExpression> getChildOfFilterNodesToCreate();

    /**
     * TODO: explain
     */
    ImmutableList<ImmutableExpression> getExpressionsToKeep();
}
