package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import org.semanticweb.ontop.pivotalrepr.NodeTransformationProposal;
import org.semanticweb.ontop.pivotalrepr.NodeTransformationProposedState;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

public class NodeTransformationProposalImpl implements NodeTransformationProposal {

    private final NodeTransformationProposedState state;
    private final Optional<QueryNode> optionalNewNode;

    public NodeTransformationProposalImpl(NodeTransformationProposedState state, QueryNode newNode) {
        switch(state) {
            case REPLACE_BY_UNIQUE_CHILD:
                break;
            case REPLACE_BY_NEW_NODE:
                break;
            case NO_CHANGE:
                throw new IllegalArgumentException("No new node has to be given when there is no change");
            case DELETE:
                throw new IllegalArgumentException("No new node has to be given when the node is deleted " +
                        "(without replacement)");
        }
        this.state = state;
        this.optionalNewNode = Optional.of(newNode);
    }

    public NodeTransformationProposalImpl(NodeTransformationProposedState state) {
        switch (state) {
            case NO_CHANGE:
                break;
            case DELETE:
                break;
            case REPLACE_BY_UNIQUE_CHILD:
            case REPLACE_BY_NEW_NODE:
                throw new IllegalArgumentException("Replacement requires giving a new node. " +
                        "Please use the other constructor.");
        }
        this.state = state;
        this.optionalNewNode = Optional.absent();
    }

    @Override
    public NodeTransformationProposedState getState() {
        return state;
    }

    @Override
    public Optional<QueryNode> getOptionalNewNode() {
        return optionalNewNode;
    }
}
