package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.JoinLikeNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

/**
 * TODO: explain
 *
 */
public interface PullVariableOutOfSubTreeResults<N extends JoinLikeNode> extends NodeCentricOptimizationResults<N> {

    QueryNode getNewSubTreeRoot() ;
}
