package it.unibz.inf.ontop.iq.proposal.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.proposal.ProjectionShrinkingProposal;

public class ProjectionShrinkingProposalImpl implements ProjectionShrinkingProposal {

    private final ExplicitVariableProjectionNode focusNode;
    private final ImmutableSet<Variable> retainedVariables;

    public ProjectionShrinkingProposalImpl(ExplicitVariableProjectionNode focusNode,
                                           ImmutableSet<Variable> retainedVariables) {
        this.focusNode = focusNode;
        this.retainedVariables = retainedVariables;
    }

    @Override
    public ExplicitVariableProjectionNode getFocusNode() {
        return focusNode;
    }

    @Override
    public ImmutableSet<Variable> getRetainedVariables() {
        return retainedVariables;
    }
}
