package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.DataNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullVariableOutOfDataNodeProposal;

public class PullVariableOutOfDataNodeProposalImpl implements PullVariableOutOfDataNodeProposal {

    private final ImmutableList<Integer> indexes;
    private final DataNode focusNode;

    public PullVariableOutOfDataNodeProposalImpl(DataNode focusNode, ImmutableList<Integer> indexes) {
        this.focusNode = focusNode;
        this.indexes = indexes;
    }

    @Override
    public ImmutableList<Integer> getIndexes() {
        return indexes;
    }

    @Override
    public DataNode getFocusNode() {
        return focusNode;
    }
}
