package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;


public class PushDownBooleanExpressionProposalImpl implements PushDownBooleanExpressionProposal {

    private final JoinOrFilterNode focusNode;
    private final ImmutableMultimap<QueryNode, ImmutableExpression> transferMap;
    private final ImmutableList<ImmutableExpression> toKeepExpressions;

    public PushDownBooleanExpressionProposalImpl(JoinOrFilterNode focusNode,
                                                 ImmutableMultimap<QueryNode, ImmutableExpression> transferMap,
                                                 ImmutableList<ImmutableExpression> toKeepExpressions) {
        this.focusNode = focusNode;
        this.transferMap = transferMap;
        this.toKeepExpressions = toKeepExpressions;
    }

    @Override
    public JoinOrFilterNode getFocusNode() {
        return focusNode;
    }

    @Override
    public ImmutableMultimap<QueryNode, ImmutableExpression> getTransferMap() {
        return transferMap;
    }

    @Override
    public ImmutableList<ImmutableExpression> getExpressionsToKeep() {
        return toKeepExpressions;
    }
}
