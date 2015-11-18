package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;
import org.semanticweb.ontop.pivotalrepr.JoinOrFilterNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.PushDownBooleanExpressionProposal;


public class PushDownBooleanExpressionProposalImpl<N extends JoinOrFilterNode> implements PushDownBooleanExpressionProposal<N> {

    private final N focusNode;
    private final ImmutableMultimap<QueryNode, ImmutableBooleanExpression> transferMap;
    private final ImmutableList<ImmutableBooleanExpression> notTransferedExpressions;

    public PushDownBooleanExpressionProposalImpl(N focusNode,
                                                 ImmutableMultimap<QueryNode, ImmutableBooleanExpression> transferMap,
                                                 ImmutableList<ImmutableBooleanExpression> notTransferedExpressions) {
        this.focusNode = focusNode;
        this.transferMap = transferMap;
        this.notTransferedExpressions = notTransferedExpressions;
    }

    @Override
    public NodeCentricOptimizationResults<N> castResults(ProposalResults results) {
        return (NodeCentricOptimizationResults<N>) results;
    }

    @Override
    public N getFocusNode() {
        return focusNode;
    }

    @Override
    public ImmutableMultimap<QueryNode, ImmutableBooleanExpression> getTransferMap() {
        return transferMap;
    }

    @Override
    public ImmutableList<ImmutableBooleanExpression> getNotTransferedExpressions() {
        return notTransferedExpressions;
    }
}
