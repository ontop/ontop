package it.unibz.inf.ontop.iq.proposal.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.proposal.FlattenUnionProposal;

public class FlattenUnionProposalImpl implements FlattenUnionProposal{

    private final UnionNode focusNode;
    private final ImmutableSet<QueryNode> childSubtreeRoots;

    public FlattenUnionProposalImpl(UnionNode focusNode, ImmutableSet<QueryNode> childSubtreeRoots) {
        this.childSubtreeRoots = childSubtreeRoots;
        this.focusNode = focusNode;
    }

    @Override
    public UnionNode getFocusNode() {
        return focusNode;
    }

    @Override
    public ImmutableSet<QueryNode> getReplacingChildSubtreeRoots() {
        return childSubtreeRoots;
    }
}
