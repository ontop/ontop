package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.ImmutableBooleanExpression;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 */
public interface PushDownBooleanExpressionProposal extends NodeCentricOptimizationProposal<JoinOrFilterNode> {

    /**
     * TODO: explain
     * TODO: find a better name
     */
    ImmutableMultimap<QueryNode, ImmutableBooleanExpression> getTransferMap();

    /**
     * TODO: explain
     */
    ImmutableList<ImmutableBooleanExpression> getExpressionsToKeep();
}
