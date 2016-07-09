package it.unibz.inf.ontop.pivotalrepr.proposal.impl;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;

import java.util.Optional;

public class ReactToChildDeletionProposalImpl implements ReactToChildDeletionProposal {

    private final QueryNode parentNode;
    private final Optional<QueryNode> optionalNextSibling;
    private final Optional<ArgumentPosition> optionalPositionOfDeletedChild;
    private final ImmutableSet<Variable> variablesProjectedByDeletedChild;

    public ReactToChildDeletionProposalImpl(QueryNode parentNode,
                                            Optional<QueryNode> optionalNextSibling,
                                            Optional<ArgumentPosition> optionalPositionOfDeletedChild,
                                            ImmutableSet<Variable> variablesProjectedByDeletedChild) {
        this.parentNode = parentNode;
        this.optionalNextSibling = optionalNextSibling;
        this.optionalPositionOfDeletedChild = optionalPositionOfDeletedChild;
        this.variablesProjectedByDeletedChild = variablesProjectedByDeletedChild;
    }

    @Override
    public QueryNode getParentNode() {
        return parentNode;
    }

    @Override
    public Optional<QueryNode> getOptionalNextSibling() {
        return optionalNextSibling;
    }

    @Override
    public Optional<ArgumentPosition> getOptionalPositionOfDeletedChild() {
        return optionalPositionOfDeletedChild;
    }

    @Override
    public ImmutableSet<Variable> getVariablesProjectedByDeletedChild() {
        return variablesProjectedByDeletedChild;
    }
}
