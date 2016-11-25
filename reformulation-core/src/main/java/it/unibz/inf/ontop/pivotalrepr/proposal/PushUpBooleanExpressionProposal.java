package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.CommutativeJoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;

import java.util.Optional;

public interface PushUpBooleanExpressionProposal extends NodeCentricOptimizationProposal<CommutativeJoinOrFilterNode, NodeCentricOptimizationResults<CommutativeJoinOrFilterNode>> {

    public Optional<JoinOrFilterNode> getRecipientNode();

    public ImmutableList<ExplicitVariableProjectionNode> getInbetweenProjectors();
}
