package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposal;
import it.unibz.inf.ontop.pivotalrepr.NodeTransformationProposedState;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

public class NodeTransformationProposalImpl implements NodeTransformationProposal {

    private final NodeTransformationProposedState state;
    private final Optional<QueryNode> optionalNewNode;
    private final ImmutableSet<Variable> nullVariables;

    public NodeTransformationProposalImpl(NodeTransformationProposedState state, QueryNode newNode,
                                          ImmutableSet<Variable> nullVariables) {
        switch(state) {
            case REPLACE_BY_UNIQUE_NON_EMPTY_CHILD:
                break;
            case REPLACE_BY_NEW_NODE:
                break;
            case NO_LOCAL_CHANGE:
                throw new IllegalArgumentException("No new node has to be given when there is no change");
            case DECLARE_AS_EMPTY:
                throw new IllegalArgumentException("No new node has to be given when the node is declared as empty");
        }
        this.state = state;
        this.optionalNewNode = Optional.of(newNode);
        this.nullVariables = nullVariables;
    }

    public NodeTransformationProposalImpl(NodeTransformationProposedState state, ImmutableSet<Variable> nullVariables) {
        switch (state) {
            case NO_LOCAL_CHANGE:
                break;
            case DECLARE_AS_EMPTY:
                break;
            case REPLACE_BY_UNIQUE_NON_EMPTY_CHILD:
            case REPLACE_BY_NEW_NODE:
                throw new IllegalArgumentException("Replacement requires giving a new node. " +
                        "Please use the other constructor.");
        }
        this.state = state;
        this.optionalNewNode = Optional.empty();
        this.nullVariables = nullVariables;
    }

    @Override
    public NodeTransformationProposedState getState() {
        return state;
    }

    @Override
    public Optional<QueryNode> getOptionalNewNode() {
        return optionalNewNode;
    }

    @Override
    public ImmutableSet<Variable> getNullVariables() {
        return nullVariables;
    }
}
