package org.semanticweb.ontop.executor.substitution;

import java.util.Optional;
import org.semanticweb.ontop.executor.NodeCentricInternalExecutor;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.QueryTreeComponent;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

import static org.semanticweb.ontop.executor.substitution.SubstitutionPropagationTools.propagateSubstitutionDown;
import static org.semanticweb.ontop.executor.substitution.SubstitutionPropagationTools.propagateSubstitutionUp;

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

        propagateSubstitutionUp(originalFocusNode, substitutionToPropagate, query, treeComponent);
        propagateSubstitutionDown(originalFocusNode, substitutionToPropagate, treeComponent);


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
