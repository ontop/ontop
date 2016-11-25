package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.CommutativeJoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

public interface PushUpBooleanExpressionProposal extends NodeCentricOptimizationProposal<CommutativeJoinOrFilterNode, NodeCentricOptimizationResults<CommutativeJoinOrFilterNode>> {

    /**
     * Recipient of the expression.
     * If empty, a new filter node recipient will be created as the child of the blocking node
     */
    public Optional<JoinOrFilterNode> getRecipientNode();

    //Node blocking further propagation
    public QueryNode getBlockingNode();

    //All nodes projecting variables on the path between provider and blocking node
    public ImmutableList<ExplicitVariableProjectionNode> getInbetweenProjectors();
}
