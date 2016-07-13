package it.unibz.inf.ontop.executor.substitution;

import java.util.Optional;

import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;

/**
 * TODO: explain
 */
public class SubstitutionPropagationExecutor<N extends QueryNode>
        implements NodeCentricInternalExecutor<N, SubstitutionPropagationProposal<N>> {

    @Override
    public NodeCentricOptimizationResults<N> apply(SubstitutionPropagationProposal<N> proposal,
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

    /**
     * TODO: explain
     *
     * TODO: refactor
     *
     */
    private NodeCentricOptimizationResults<N> applySubstitution(SubstitutionPropagationProposal<N> proposal,
                                                                        IntermediateQuery query,
                                                                        QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException, EmptyQueryException {
        N originalFocusNode = proposal.getFocusNode();
        ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate = proposal.getSubstitution();

        /**
         * TODO: check the results!!!
         */
        SubstitutionPropagationTools.propagateSubstitutionUp(originalFocusNode, substitutionToPropagate, query, treeComponent);
        SubstitutionPropagationTools.propagateSubstitutionDown(originalFocusNode, substitutionToPropagate, query,
                treeComponent);


        N newQueryNode = propagateToFocusNode(originalFocusNode, substitutionToPropagate, query, treeComponent);

        /**
         * The substitution is supposed
         */
        return new NodeCentricOptimizationResultsImpl<>(query, newQueryNode);
    }



    private static <N extends QueryNode> N propagateToFocusNode(
            N originalFocusNode, ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate,
            IntermediateQuery query, QueryTreeComponent treeComponent) throws QueryNodeSubstitutionException {

        SubstitutionResults<? extends QueryNode> substitutionResults =
                originalFocusNode.applyDescendingSubstitution(substitutionToPropagate, query);
        Optional<? extends QueryNode> optionalNewFocusNode = substitutionResults.getOptionalNewNode();
        if (optionalNewFocusNode.isPresent()) {
            QueryNode newFocusNode = optionalNewFocusNode.get();
            if (originalFocusNode != newFocusNode) {
                treeComponent.replaceNode(originalFocusNode, newFocusNode);
            }
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
