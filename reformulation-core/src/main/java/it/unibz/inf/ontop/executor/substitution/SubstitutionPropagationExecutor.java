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

import java.util.Optional;

import static it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationTools.*;

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
        NodeCentricOptimizationResults<N> ascendingPropagationResults = propagateSubstitutionUp(originalFocusNode,
                substitutionToPropagate, query, treeComponent);

        /**
         * If some ancestors are removed, don't go further
         */
        if (!ascendingPropagationResults.getOptionalNewNode().isPresent()) {
            return ascendingPropagationResults;
        }

        if (ascendingPropagationResults.getOptionalNewNode().get() != originalFocusNode) {
            throw new IllegalStateException("The original focus node was not expected to changed");
        }

        /**
         * Then to the focus node
         */
        SubstitutionApplicationResults<N> localApplicationResults = applySubstitutionToNode(originalFocusNode,
                substitutionToPropagate, query, treeComponent);

        /**
         * Finally, down
         */
        Optional<QueryNode> optionalNewFocusNode = localApplicationResults.getNewNodeOrReplacingChild();
        if (optionalNewFocusNode.isPresent()) {

            if (localApplicationResults.getOptionalSubstitution().isPresent()) {
                ImmutableSubstitution<? extends ImmutableTerm> newSubstitution = localApplicationResults.getOptionalSubstitution().get();

                /**
                 * TODO: analyse the results!
                 */
                propagateSubstitutionDown(optionalNewFocusNode.get(), newSubstitution, query, treeComponent);

                // TODO: refactor
                if (localApplicationResults.getOptionalNewNode().isPresent()) {
                    return new NodeCentricOptimizationResultsImpl<>(query, localApplicationResults.getOptionalNewNode().get());
                }
                else if (localApplicationResults.isReplacedByAChild()) {
                    return new NodeCentricOptimizationResultsImpl<>(query, localApplicationResults.getOptionalReplacingChild());
                }
                /**
                 * Replaced by another node
                 */
                else {
                    QueryNode replacingNode = localApplicationResults.getNewNodeOrReplacingChild().get();

                    return new NodeCentricOptimizationResultsImpl<>(query,
                            query.getNextSibling(replacingNode),
                            query.getParent(replacingNode));
                }
            }
            else if (localApplicationResults.getOptionalNewNode().isPresent()) {
                return new NodeCentricOptimizationResultsImpl<>(query, localApplicationResults.getOptionalNewNode().get());
            }
            else if (localApplicationResults.isReplacedByAChild()) {
                return new NodeCentricOptimizationResultsImpl<>(query, localApplicationResults.getOptionalReplacingChild());
            }
            /**
             * Replaced by another node
             */
            else {
                QueryNode replacingNode = localApplicationResults.getNewNodeOrReplacingChild().get();

                return new NodeCentricOptimizationResultsImpl<>(query,
                        query.getNextSibling(replacingNode),
                        query.getParent(replacingNode));
            }

        }
        /**
         *  The focus node has removed
         *
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, localApplicationResults.getOptionalNextSibling(),
                    localApplicationResults.getOptionalClosestAncestor());
        }

    }
}
