package it.unibz.inf.ontop.executor.substitution;


import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionUpPropagationProposal;

public class SubstitutionUpPropagationExecutor <N extends QueryNode>
        implements NodeCentricInternalExecutor<N, SubstitutionUpPropagationProposal<N>> {

    @Override
    public NodeCentricOptimizationResults<N> apply(SubstitutionUpPropagationProposal<N> proposal,
                                                   IntermediateQuery query,
                                                   QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, EmptyQueryException {
        try {
            return applySubstitution(proposal, query, treeComponent);
        }
        catch (QueryNodeSubstitutionException e) {
            throw new InvalidQueryOptimizationProposalException(e.getMessage());
        }
    }

    private NodeCentricOptimizationResults<N> applySubstitution(SubstitutionUpPropagationProposal<N> proposal,
                                                                IntermediateQuery query,
                                                                QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException, EmptyQueryException {
        N originalFocusNode = proposal.getFocusNode();
        ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate = proposal.getAscendingSubstitution();

        return SubstitutionPropagationTools.propagateSubstitutionUp(originalFocusNode, substitutionToPropagate, query,
                treeComponent);
    }
}
