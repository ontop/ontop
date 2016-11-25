package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushUpBooleanExpressionProposal;

import java.util.Optional;

public class PushUpBooleanExpressionProposalImpl implements PushUpBooleanExpressionProposal {

    //Node providing the expression to pushed up
    private final CommutativeJoinOrFilterNode providerNode;

    private final QueryNode blockingNode;
    private final Optional<JoinOrFilterNode> recipientNode;
    private final ImmutableList<ExplicitVariableProjectionNode> inbetweenProjectors;

    public PushUpBooleanExpressionProposalImpl(CommutativeJoinOrFilterNode providerNode,
                                               QueryNode blockingNode,
                                               Optional<JoinOrFilterNode> recipientNode,
                                               ImmutableList<ExplicitVariableProjectionNode> inbetweenProjectors) {
        if (!(
                (blockingNode instanceof UnionNode) ||
                        (blockingNode instanceof ConstructionNode) ||
                        (blockingNode instanceof LeftJoinNode)
        )){
         throw new IllegalStateException("a node blocking up propagation of a boolean expression must be a ConstructionNode," +
                 "UnionNode or LeftJoinNode");
        }
        this.blockingNode = blockingNode;
        this.providerNode = providerNode;
        this.recipientNode = recipientNode;
        this.inbetweenProjectors = inbetweenProjectors;
    }


    public Optional<JoinOrFilterNode> getRecipientNode() {
        return recipientNode;
    }

    @Override
    public QueryNode getBlockingNode() {
        return blockingNode;
    }

    public ImmutableList<ExplicitVariableProjectionNode> getInbetweenProjectors() {
        return inbetweenProjectors;
    }

    @Override
    public CommutativeJoinOrFilterNode getFocusNode() {
        return providerNode;
    }
}
