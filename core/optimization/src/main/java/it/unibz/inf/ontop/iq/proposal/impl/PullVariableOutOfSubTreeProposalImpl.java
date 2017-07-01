package it.unibz.inf.ontop.iq.proposal.impl;


import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.PullVariableOutOfSubTreeProposal;

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
