package it.unibz.inf.ontop.executor.substitution;

import it.unibz.inf.ontop.executor.SimpleNodeCentricInternalExecutor;
import it.unibz.inf.ontop.executor.substitution.LocalPropagationTools.SubstitutionApplicationResults;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.SubstitutionPropagationProposal;

import java.util.Optional;

import static it.unibz.inf.ontop.executor.substitution.AscendingPropagationTools.*;
import static it.unibz.inf.ontop.executor.substitution.DescendingPropagationTools.propagateSubstitutionDown;
import static it.unibz.inf.ontop.executor.substitution.LocalPropagationTools.applySubstitutionToNode;

/**
 * TODO: explain
 */
public class SubstitutionPropagationExecutor<N extends QueryNode>
        implements SimpleNodeCentricInternalExecutor<N, SubstitutionPropagationProposal<N>> {

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
                substitutionToPropagate, query, treeComponent, Optional.empty());

        /**
         * If some ancestors are removed, does not go further
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
         * Finally, propagates down and returns the results
         */
        return propagateDown(query, treeComponent, localApplicationResults);


    }

    private NodeCentricOptimizationResults<N> propagateDown(IntermediateQuery query, QueryTreeComponent treeComponent,
                                                            SubstitutionApplicationResults<N> localApplicationResults)
            throws EmptyQueryException {

        if (localApplicationResults.getNewNodeOrReplacingChild().isPresent()) {

            /**
             * Still a substitution to propagate down
             */
            if (localApplicationResults.getOptionalSubstitution().isPresent()) {
                ImmutableSubstitution<? extends ImmutableTerm> newSubstitution = localApplicationResults.getOptionalSubstitution().get();

                Optional<N> optionalNewFocusNode = localApplicationResults.getOptionalNewNode();

                if (optionalNewFocusNode.isPresent()) {
                    return propagateSubstitutionDown(optionalNewFocusNode.get(), newSubstitution, query, treeComponent);
                }
                /**
                 * When the focus has already been replaced by its child
                 */
                else  {
                    QueryNode replacingNode = localApplicationResults.getOptionalReplacingChild().get();

                    /**
                     * The results have to be converted
                     */
                    NodeCentricOptimizationResults<QueryNode> descendingResults = propagateSubstitutionDown(
                            replacingNode, newSubstitution, query, treeComponent);

                    if (descendingResults.getNewNodeOrReplacingChild().isPresent()) {
                        // Declares as replacing child
                        return new NodeCentricOptimizationResultsImpl<>(query, descendingResults.getNewNodeOrReplacingChild());
                    } else {
                        return new NodeCentricOptimizationResultsImpl<>(query, descendingResults.getOptionalNextSibling(),
                                descendingResults.getOptionalClosestAncestor());
                    }
                }
            }
            /**
             * No propagation down
             */
            else {
                return localApplicationResults.getOptionalNewNode()
                        .map(focus -> new NodeCentricOptimizationResultsImpl<>(query, focus))
                        .orElseGet(() -> new NodeCentricOptimizationResultsImpl<>(query,
                                localApplicationResults.getOptionalReplacingChild()));
            }
        }
        /**
         *  The focus node has removed by the local application
         *
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, localApplicationResults.getOptionalNextSibling(),
                    localApplicationResults.getOptionalClosestAncestor());
        }
    }
}
