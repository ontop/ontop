package org.semanticweb.ontop.pivotalrepr.proposal.impl;

import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.ProposalResults;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;

public class SubstitutionPropagationProposalImpl implements SubstitutionPropagationProposal {

    private final QueryNode focusNode;
    private final ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate;

    public SubstitutionPropagationProposalImpl(QueryNode focusNode,
                                               ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate) {
        this.focusNode = focusNode;
        this.substitutionToPropagate = substitutionToPropagate;
    }

    @Override
    public ImmutableSubstitution<VariableOrGroundTerm> getSubstitution() {
        return substitutionToPropagate;
    }

    @Override
    public NodeCentricOptimizationResults castResults(ProposalResults results) {
        return (NodeCentricOptimizationResults) results;
    }

    @Override
    public QueryNode getFocusNode() {
        return focusNode;
    }
}
