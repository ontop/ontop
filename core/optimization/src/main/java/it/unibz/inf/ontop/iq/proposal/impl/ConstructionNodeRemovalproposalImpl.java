package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeRemovalProposal;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

public class ConstructionNodeRemovalproposalImpl implements ConstructionNodeRemovalProposal {

    private final ConstructionNode focusNode;
    private final ImmutableSubstitution substitution;
    private final QueryNode childSubtreeRoot;


    public ConstructionNodeRemovalproposalImpl(ConstructionNode focusNode, ImmutableSubstitution substitution, QueryNode childSubtreeRoot) {
        this.focusNode = focusNode;
        this.substitution = substitution;
        this.childSubtreeRoot = childSubtreeRoot;
    }

    @Override
    public ImmutableSubstitution getSubstitution() {
        return substitution;
    }

    @Override
    public ConstructionNode getFocusNode() {
        return focusNode;
    }

    public QueryNode getChildSubtreeRoot() {
        return childSubtreeRoot;
    }
}
