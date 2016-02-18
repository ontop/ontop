package unibz.inf.ontop.executor.substitution;

import java.util.Optional;

import unibz.inf.ontop.model.VariableOrGroundTerm;
import unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import unibz.inf.ontop.pivotalrepr.QueryNode;
import unibz.inf.ontop.pivotalrepr.QueryNodeSubstitutionException;
import unibz.inf.ontop.pivotalrepr.SubstitutionResults;
import unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import unibz.inf.ontop.model.ImmutableSubstitution;
import unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;

/**
 * TODO: explain
 */
public class SubstitutionPropagationExecutor<N extends QueryNode>
        implements NodeCentricInternalExecutor<N, SubstitutionPropagationProposal<N>> {

    @Override
    public NodeCentricOptimizationResults<N> apply(SubstitutionPropagationProposal<N> proposal,
                                                   IntermediateQuery query,
                                                   QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        try {
            return applySubstitution(proposal, query, treeComponent);
        }
        catch (QueryNodeSubstitutionException e) {
            throw new InvalidQueryOptimizationProposalException(e.getMessage());
        }
    }

    /**
     * TODO: explain
     *
     */
    private NodeCentricOptimizationResults<N> applySubstitution(SubstitutionPropagationProposal<N> proposal,
                                                                        IntermediateQuery query,
                                                                        QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException {
        N originalFocusNode = proposal.getFocusNode();
        ImmutableSubstitution<? extends VariableOrGroundTerm> substitutionToPropagate = proposal.getSubstitution();

        SubstitutionPropagationTools.propagateSubstitutionUp(originalFocusNode, substitutionToPropagate, query, treeComponent);
        SubstitutionPropagationTools.propagateSubstitutionDown(originalFocusNode, substitutionToPropagate, treeComponent);


        N newQueryNode = propagateToFocusNode(originalFocusNode, substitutionToPropagate, treeComponent);

        /**
         * The substitution is supposed
         */
        return new NodeCentricOptimizationResultsImpl<>(query, newQueryNode);
    }



    private static <N extends QueryNode> N propagateToFocusNode(N originalFocusNode,
                                          ImmutableSubstitution<? extends VariableOrGroundTerm> substitutionToPropagate,
                                          QueryTreeComponent treeComponent) throws QueryNodeSubstitutionException {

        SubstitutionResults<? extends QueryNode> substitutionResults =
                originalFocusNode.applyDescendentSubstitution(substitutionToPropagate);
        Optional<? extends QueryNode> optionalNewFocusNode = substitutionResults.getOptionalNewNode();
        if (optionalNewFocusNode.isPresent()) {
            QueryNode newFocusNode = optionalNewFocusNode.get();
            treeComponent.replaceNode(originalFocusNode, newFocusNode);
            return (N) newFocusNode;
        }
        /**
         * TODO: should we handle this case properly?
         */
        else {
            throw new RuntimeException("The focus node was not expected to become not needed anymore ");
        }
    }
}
