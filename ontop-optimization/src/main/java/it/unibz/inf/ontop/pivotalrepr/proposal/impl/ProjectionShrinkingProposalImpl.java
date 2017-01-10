package it.unibz.inf.ontop.pivotalrepr.proposal.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.proposal.ProjectionShrinkingProposal;

public class ProjectionShrinkingProposalImpl implements ProjectionShrinkingProposal {
    private IntermediateQuery query;
    private ExplicitVariableProjectionNode focusNode;
    private ImmutableSet<Variable> retainedVariables;

    public ProjectionShrinkingProposalImpl(IntermediateQuery query, ExplicitVariableProjectionNode focusNode,
                                           ImmutableSet<Variable> retainedVariables) {
        this.query = query;
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
