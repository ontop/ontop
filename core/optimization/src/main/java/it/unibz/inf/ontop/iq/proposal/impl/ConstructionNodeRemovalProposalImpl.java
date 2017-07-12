package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeRemovalProposal;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

public class ConstructionNodeRemovalProposalImpl implements ConstructionNodeRemovalProposal {

    private final ConstructionNode focusNode;
    private final ImmutableSubstitution substitution;
    private final QueryNode childSubtreeRoot;
    private final boolean deleteConstructionNodeChain;


    public ConstructionNodeRemovalProposalImpl(ConstructionNode focusNode, ImmutableSubstitution substitution, QueryNode childSubtreeRoot, boolean deleteConstructionNodeChain) {
        this.focusNode = focusNode;
        this.substitution = substitution;
        this.childSubtreeRoot = childSubtreeRoot;
        this.deleteConstructionNodeChain = deleteConstructionNodeChain;
    }

    @Override
    public ImmutableSubstitution getSubstitution() {
        return substitution;
    }

    @Override
    public boolean deleteConstructionNodeChain() {
        return deleteConstructionNodeChain;
    }

    @Override
    public ConstructionNode getFocusNode() {
        return focusNode;
    }

    public QueryNode getChildSubtreeRoot() {
        return childSubtreeRoot;
    }
}
