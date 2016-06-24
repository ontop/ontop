package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;


public class PushDownBooleanExpressionProposalImpl implements PushDownBooleanExpressionProposal {
    private final JoinOrFilterNode focusNode;
    private final ImmutableMultimap<JoinOrFilterNode, ImmutableExpression> directRecipients;
    private final ImmutableMultimap<QueryNode, ImmutableExpression> childOfFilterNodesToCreate;
    private final ImmutableList<ImmutableExpression> expressionsToKeep;

    public PushDownBooleanExpressionProposalImpl(
            JoinOrFilterNode focusNode, ImmutableMultimap<JoinOrFilterNode, ImmutableExpression> directRecipients,
            ImmutableMultimap<QueryNode, ImmutableExpression> childOfFilterNodesToCreate,
            ImmutableList<ImmutableExpression> expressionsToKeep) {

        this.focusNode = focusNode;
        this.directRecipients = directRecipients;
        this.childOfFilterNodesToCreate = childOfFilterNodesToCreate;
        this.expressionsToKeep = expressionsToKeep;
    }

    @Override
    public ImmutableMultimap<JoinOrFilterNode, ImmutableExpression> getDirectRecipients() {
        return directRecipients;
    }

    @Override
    public ImmutableMultimap<QueryNode, ImmutableExpression> getChildOfFilterNodesToCreate() {
        return childOfFilterNodesToCreate;
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
