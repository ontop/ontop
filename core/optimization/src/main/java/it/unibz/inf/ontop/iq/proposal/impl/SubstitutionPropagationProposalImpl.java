package it.unibz.inf.ontop.iq.proposal.impl;

import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

public class SubstitutionPropagationProposalImpl<T extends QueryNode> implements SubstitutionPropagationProposal<T> {

    private final T focusNode;
    private final ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate;

    public SubstitutionPropagationProposalImpl(
            T focusNode, ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate) {
        this(focusNode, substitutionToPropagate, true);
    }

    public SubstitutionPropagationProposalImpl(T focusNode,
                                               ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate,
                                               boolean shouldCheckProjection) {
        this.focusNode = focusNode;
        this.substitutionToPropagate = substitutionToPropagate;

        if (shouldCheckProjection) {
            checkProjection();
        }
    }

    private void checkProjection() {
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