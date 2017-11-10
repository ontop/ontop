package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;

/**
 * TODO: explain
 *
 */
public interface PullVariableOutOfSubTreeResults<N extends JoinLikeNode> extends NodeCentricOptimizationResults<N> {

    QueryNode getNewSubTreeRoot() ;
}
