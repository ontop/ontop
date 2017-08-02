package it.unibz.inf.ontop.iq.proposal.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.iq.node.CommutativeJoinOrFilterNode;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.PushDownBooleanExpressionProposal;


public class PushDownBooleanExpressionProposalImpl implements PushDownBooleanExpressionProposal {
    private final JoinOrFilterNode focusNode;
    private final ImmutableMultimap<CommutativeJoinOrFilterNode, ImmutableExpression> newDirectRecipientNodes;
    private final ImmutableMultimap<QueryNode, ImmutableExpression> indirectRecipientNodes;
    private final ImmutableList<ImmutableExpression> expressionsToKeep;

    public PushDownBooleanExpressionProposalImpl(
            JoinOrFilterNode focusNode, ImmutableMultimap<CommutativeJoinOrFilterNode, ImmutableExpression> newDirectRecipientNodes,
            ImmutableMultimap<QueryNode, ImmutableExpression> indirectRecipientNodes,
            ImmutableList<ImmutableExpression> expressionsToKeep) {

        this.focusNode = focusNode;
        this.newDirectRecipientNodes = newDirectRecipientNodes;
        this.indirectRecipientNodes = indirectRecipientNodes;
        this.expressionsToKeep = expressionsToKeep;
    }

    @Override
    public ImmutableMultimap<CommutativeJoinOrFilterNode, ImmutableExpression> getNewDirectRecipientNodes() {
        return newDirectRecipientNodes;
    }

    @Override
    public ImmutableMultimap<QueryNode, ImmutableExpression> getIndirectRecipientNodes() {
        return indirectRecipientNodes;
    }

    @Override
    public ImmutableList<ImmutableExpression> getExpressionsToKeep() {
        return expressionsToKeep;
    }

    @Override
    public JoinOrFilterNode getFocusNode() {
        return focusNode;
    }
}
