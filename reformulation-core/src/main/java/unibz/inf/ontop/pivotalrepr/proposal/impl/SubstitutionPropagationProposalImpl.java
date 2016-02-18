package unibz.inf.ontop.pivotalrepr.proposal.impl;

import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.pivotalrepr.QueryNode;
import unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import unibz.inf.ontop.model.ImmutableSubstitution;

public class SubstitutionPropagationProposalImpl<T extends QueryNode> implements SubstitutionPropagationProposal<T> {

    private final T focusNode;
    private final ImmutableSubstitution<? extends VariableOrGroundTerm> substitutionToPropagate;

    public SubstitutionPropagationProposalImpl(
            T focusNode, ImmutableSubstitution<? extends VariableOrGroundTerm> substitutionToPropagate) {
        this.focusNode = focusNode;
        this.substitutionToPropagate = substitutionToPropagate;
    }

    @Override
    public ImmutableSubstitution<? extends VariableOrGroundTerm> getSubstitution() {
        return substitutionToPropagate;
    }

    @Override
    public T getFocusNode() {
        return focusNode;
    }
}
