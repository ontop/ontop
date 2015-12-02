package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.SubTreeDelimiterNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.PullOutVariableProposal;

public class PullOutVariableProposalImpl  implements PullOutVariableProposal {

    private final ImmutableList<Integer> indexes;
    private final SubTreeDelimiterNode focusNode;

    public PullOutVariableProposalImpl(SubTreeDelimiterNode focusNode, ImmutableList<Integer> indexes) {
        this.focusNode = focusNode;
        this.indexes = indexes;
    }

    @Override
    public ImmutableList<Integer> getIndexes() {
        return indexes;
    }

    @Override
    public NodeCentricOptimizationResults<SubTreeDelimiterNode> castResults(ProposalResults results) {
        return (NodeCentricOptimizationResults<SubTreeDelimiterNode>)results;
    }

    @Override
    public SubTreeDelimiterNode getFocusNode() {
        return focusNode;
    }
}
