package org.semanticweb.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.JoinOrFilterNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

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
