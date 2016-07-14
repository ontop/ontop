package it.unibz.inf.ontop.executor.substitution;

import it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationTools.SubstitutionApplicationResults;
import it.unibz.inf.ontop.model.ImmutableTerm;
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
         * First propagates up
         */
        SubstitutionPropagationTools.propagateSubstitutionUp(originalFocusNode, substitutionToPropagate, query,
                treeComponent);

        /**
         * Then to the focus node
         */
        SubstitutionApplicationResults<N> newNodeAndSubst = SubstitutionPropagationTools.applySubstitutionToNode(
                originalFocusNode, substitutionToPropagate, query, treeComponent);

        /**
         * Finally, down
         */
        if (newNodeAndSubst.getOptionalSubstitution().isPresent()) {
            ImmutableSubstitution<? extends ImmutableTerm> newSubstitution = newNodeAndSubst.getOptionalSubstitution().get();

            SubstitutionPropagationTools.propagateSubstitutionDown(newNodeAndSubst.getNewOrReplacingNode(), newSubstitution, query,
                    treeComponent);
        }

        if (newNodeAndSubst.getNewNode().isPresent()) {
            return new NodeCentricOptimizationResultsImpl<N>(query, newNodeAndSubst.getNewNode().get());
        }
        else if (newNodeAndSubst.isReplacedByAChild()) {
            return new NodeCentricOptimizationResultsImpl<N>(query, newNodeAndSubst.getReplacingNode());
        }
        /**
         * Replaced by another node
         */
        else {
            QueryNode replacingNode = newNodeAndSubst.getReplacingNode().get();

            return new NodeCentricOptimizationResultsImpl<N>(query,
                    query.getNextSibling(replacingNode),
                    query.getParent(replacingNode));
        }
    }
}
