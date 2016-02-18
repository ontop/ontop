package unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import unibz.inf.ontop.model.ImmutableBooleanExpression;
import unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import unibz.inf.ontop.pivotalrepr.QueryNode;

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
