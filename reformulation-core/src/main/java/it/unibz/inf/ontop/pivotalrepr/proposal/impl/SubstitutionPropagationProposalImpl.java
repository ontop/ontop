package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.model.ImmutableSubstitution;

public class SubstitutionPropagationProposalImpl<T extends QueryNode> implements SubstitutionPropagationProposal<T> {

    private final T focusNode;
    private final ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate;

    public SubstitutionPropagationProposalImpl(
            T focusNode, ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate) {
        this.focusNode = focusNode;
        this.substitutionToPropagate = substitutionToPropagate;

        checkValidity();
    }

    private void checkValidity() {
        if (focusNode instanceof ExplicitVariableProjectionNode) {
            ExplicitVariableProjectionNode node = (ExplicitVariableProjectionNode) focusNode;

            if (!node.getVariables().containsAll(substitutionToPropagate.getDomain())) {
                throw new InvalidQueryOptimizationProposalException("Only variables projected by "  + focusNode
                + " can be propagated.\n Invalid substitution was " + substitutionToPropagate);
            }
        }
    }

    @Override
    public ImmutableSubstitution<? extends ImmutableTerm> getSubstitution() {
        return substitutionToPropagate;
    }

    @Override
    public T getFocusNode() {
        return focusNode;
    }

    @Override
    public String toString() {
        return "Propagation " + substitutionToPropagate + " from " + focusNode;
    }
}