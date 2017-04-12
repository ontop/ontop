package it.unibz.inf.ontop.pivotalrepr.proposal.impl;


import it.unibz.inf.ontop.model.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.pivotalrepr.JoinLikeNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.PullVariableOutOfSubTreeProposal;

public class PullVariableOutOfSubTreeProposalImpl<T extends JoinLikeNode> implements PullVariableOutOfSubTreeProposal<T> {

    private final T focusNode;
    private final InjectiveVar2VarSubstitution substitution;
    private final QueryNode subTreeRootNode;

    public PullVariableOutOfSubTreeProposalImpl(T focusNode, InjectiveVar2VarSubstitution substitution,
                                                QueryNode subTreeRootNode) {
        this.focusNode = focusNode;
        this.substitution = substitution;
        this.subTreeRootNode = subTreeRootNode;
    }

    @Override
    public InjectiveVar2VarSubstitution getRenamingSubstitution() {
        return substitution;
    }

    @Override
    public QueryNode getSubTreeRootNode() {
        return subTreeRootNode;
    }

    @Override
    public T getFocusNode() {
        return focusNode;
    }
}
