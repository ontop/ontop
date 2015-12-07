package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;

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
