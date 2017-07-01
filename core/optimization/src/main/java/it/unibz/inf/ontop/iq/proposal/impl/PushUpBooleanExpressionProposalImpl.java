package it.unibz.inf.ontop.iq.proposal.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.node.CommutativeJoinOrFilterNode;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.iq.proposal.PushUpBooleanExpressionProposal;

import java.util.Optional;

public class PushUpBooleanExpressionProposalImpl implements PushUpBooleanExpressionProposal {

    private final ImmutableExpression propagatedExpression;
    private final ImmutableMap<CommutativeJoinOrFilterNode, Optional<ImmutableExpression>> providerToNonPropagatedExpression;
    private final QueryNode upMostPropagatingNode;
    private final Optional<JoinOrFilterNode> recipientNode;

    private final ImmutableList<ExplicitVariableProjectionNode> inbetweenProjectors;

    public PushUpBooleanExpressionProposalImpl(ImmutableExpression propagatedExpression,
                                               ImmutableMap<CommutativeJoinOrFilterNode, Optional<ImmutableExpression>> providerToNonPropagatedExpression,
                                               QueryNode upMostPropagatingNode,
                                               Optional<JoinOrFilterNode> recipientNode,
                                               ImmutableList<ExplicitVariableProjectionNode> inbetweenProjectors) {
        this.propagatedExpression = propagatedExpression;
        this.providerToNonPropagatedExpression = providerToNonPropagatedExpression;
        this.upMostPropagatingNode = upMostPropagatingNode;
        this.recipientNode = recipientNode;
        this.inbetweenProjectors = inbetweenProjectors;
    }

    public Optional<JoinOrFilterNode> getRecipientNode() {
        return recipientNode;
    }

    @Override
    public QueryNode getUpMostPropagatingNode() {
        return upMostPropagatingNode;
    }

    public ImmutableList<ExplicitVariableProjectionNode> getInbetweenProjectors() {
        return inbetweenProjectors;
    }

    @Override
    public ImmutableExpression getPropagatedExpression() {
        return propagatedExpression;
    }

    public ImmutableMap<CommutativeJoinOrFilterNode, Optional<ImmutableExpression>> getProviderToNonPropagatedExpression() {
        return providerToNonPropagatedExpression;
    }
}
