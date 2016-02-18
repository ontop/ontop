package unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import unibz.inf.ontop.model.ImmutableBooleanExpression;
import unibz.inf.ontop.pivotalrepr.JoinOrFilterNode;
import unibz.inf.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;
import unibz.inf.ontop.pivotalrepr.QueryNode;


public class PushDownBooleanExpressionProposalImpl implements PushDownBooleanExpressionProposal {

    private final JoinOrFilterNode focusNode;
    private final ImmutableMultimap<QueryNode, ImmutableBooleanExpression> transferMap;
    private final ImmutableList<ImmutableBooleanExpression> toKeepExpressions;

    public PushDownBooleanExpressionProposalImpl(JoinOrFilterNode focusNode,
                                                 ImmutableMultimap<QueryNode, ImmutableBooleanExpression> transferMap,
                                                 ImmutableList<ImmutableBooleanExpression> toKeepExpressions) {
        this.focusNode = focusNode;
        this.transferMap = transferMap;
        this.toKeepExpressions = toKeepExpressions;
    }

    @Override
    public JoinOrFilterNode getFocusNode() {
        return focusNode;
    }

    @Override
    public ImmutableMultimap<QueryNode, ImmutableBooleanExpression> getTransferMap() {
        return transferMap;
    }

    @Override
    public ImmutableList<ImmutableBooleanExpression> getExpressionsToKeep() {
        return toKeepExpressions;
    }
}
