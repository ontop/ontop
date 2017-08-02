package it.unibz.inf.ontop.iq.proposal.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.proposal.FlattenUnionProposal;

public class FlattenUnionProposalImpl implements FlattenUnionProposal {

    private final UnionNode focusNode;
    private final ImmutableSet<QueryNode> childSubQueryRoots;

    public FlattenUnionProposalImpl(UnionNode focusNode, ImmutableSet<QueryNode> childSubQueryRoots) {
        this.childSubQueryRoots = childSubQueryRoots;
        this.focusNode = focusNode;
    }

    @Override
    public UnionNode getFocusNode() {
        return focusNode;
    }

    @Override
    public ImmutableSet<QueryNode> getSubqueryRoots() {
        return childSubQueryRoots;
    }
}

