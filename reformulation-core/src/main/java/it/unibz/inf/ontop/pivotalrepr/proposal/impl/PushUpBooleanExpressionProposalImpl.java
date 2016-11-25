package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.CommutativeJoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.pivotalrepr.JoinLikeNode;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushUpBooleanExpressionProposal;

import java.util.Optional;

public class PushUpBooleanExpressionProposalImpl implements PushUpBooleanExpressionProposal{

    //Node providing the expression to pushed up
    private final CommutativeJoinOrFilterNode providerNode;
    /**
     * Recipient of the expression.
     * If empty, a new filter node recipient will be created as the child of the root ConstructionNode
     */
    private final Optional<JoinOrFilterNode> recipientNode;
    //All nodes projecting variables on the path between provider and recipient node
    private final ImmutableList<ExplicitVariableProjectionNode> inbetweenProjectors;

    public PushUpBooleanExpressionProposalImpl(CommutativeJoinOrFilterNode providerNode, Optional<JoinOrFilterNode> recipientNode,
                                               ImmutableList<ExplicitVariableProjectionNode> inbetweenProjectors) {
        this.providerNode = providerNode;
        this.recipientNode = recipientNode;
        this.inbetweenProjectors = inbetweenProjectors;
    }


    public  Optional<JoinOrFilterNode> getRecipientNode() {
        return recipientNode;
    }

    public ImmutableList<ExplicitVariableProjectionNode> getInbetweenProjectors() {
        return inbetweenProjectors;
    }

    @Override
    public CommutativeJoinOrFilterNode getFocusNode() {
        return providerNode;
    }
}
