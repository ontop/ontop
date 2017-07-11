package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeRemovalProposal;

public class ConstructionNodeRemovalproposalImpl implements ConstructionNodeRemovalProposal {

    private final ConstructionNode focusNode;
    private final ConstructionNode replacingNode;
    private final QueryNode childSubtreeRoot;


    public ConstructionNodeRemovalproposalImpl(ConstructionNode focusNode, ConstructionNode replacingNode, QueryNode childSubtreeRoot) {
        this.focusNode = focusNode;
        this.replacingNode = replacingNode;
        this.childSubtreeRoot = childSubtreeRoot;
    }

    @Override
    public ConstructionNode getFocusNode() {
        return focusNode;
    }

    @Override
    public ConstructionNode getReplacingNode() {
        return replacingNode;
    }

    public QueryNode getChildSubtreeRoot() {
        return childSubtreeRoot;
    }
}
