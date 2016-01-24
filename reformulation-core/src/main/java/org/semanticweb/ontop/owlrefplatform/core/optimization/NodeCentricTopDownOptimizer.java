package org.semanticweb.ontop.owlrefplatform.core.optimization;

import org.semanticweb.ontop.pivotalrepr.EmptyQueryException;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

import java.util.Optional;

/**
 *
 */
public abstract class NodeCentricTopDownOptimizer<P extends NodeCentricOptimizationProposal<? extends QueryNode>>
        extends TopDownOptimizer {

    private final boolean canEmptyQuery;

    protected NodeCentricTopDownOptimizer(boolean canEmptyQuery) {
        this.canEmptyQuery = canEmptyQuery;
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        try {
            return optimizeQuery(query);
        } catch (EmptyQueryException e) {
            if (canEmptyQuery) {
                throw e;
            }
            else {
                throw new IllegalStateException("Inconsistency: " + this + " should not empty the query");
            }
        }
    }

    private IntermediateQuery optimizeQuery(IntermediateQuery initialQuery) throws EmptyQueryException {

        // Non-final
        Optional<QueryNode> optionalNextNode = Optional.of((QueryNode)initialQuery.getRootConstructionNode());

        // Non-final
        IntermediateQuery currentQuery = initialQuery;

        while (optionalNextNode.isPresent()) {
            QueryNode currentNode = optionalNextNode.get();

            Optional<P> optionalProposal = evaluateNode(currentNode);

            if (optionalProposal.isPresent()) {
                NodeCentricOptimizationResults<? extends QueryNode> optimizationResults = currentQuery.applyProposal(
                        optionalProposal.get());

                currentQuery = optimizationResults.getResultingQuery();
                optionalNextNode = getNextNodeFromOptimizationResults(optimizationResults);
            }
            else {
                optionalNextNode = getNaturalNextNode(currentQuery, currentNode);
            }
        }
        return currentQuery;
    }

    protected abstract Optional<P> evaluateNode(QueryNode node);

    /**
     * TODO: explain
     *
     */
    private Optional<QueryNode> getNextNodeFromOptimizationResults(NodeCentricOptimizationResults<? extends QueryNode>
                                                                           optimizationResults) {
        IntermediateQuery query = optimizationResults.getResultingQuery();

        /**
         * First look at the "new current node" (if any)
         */
        Optional<? extends QueryNode> optionalNewCurrentNode = optimizationResults.getOptionalNewNode();
        if (optionalNewCurrentNode.isPresent()) {
            return optionalNewCurrentNode.flatMap(n -> getNaturalNextNode(query, n));
        }
        /**
         * The current node (and thus its sub-tree) is not part of the query anymore.
         */
        else {
            Optional<QueryNode> optionalNextSibling = optimizationResults.getOptionalNextSibling();

            /**
             * Looks first for the next sibling
             */
            if (optionalNextSibling.isPresent()) {
                return optionalNextSibling;
            } else {
                Optional<QueryNode> optionalAncestor = optimizationResults.getOptionalClosestAncestor();
                /**
                 * If no sibling of the optimized node, looks for a sibling of an ancestor.
                 */
                if (optionalAncestor.isPresent()) {
                    return getNextNodeSameOrUpperLevel(query, optionalAncestor.get());
                }
                /**
                 * No ancestor ---> should have thrown an EmptyQueryException
                 */
                else {
                    throw new IllegalStateException("No ancestor --> " +
                            "an EmptyQueryException should have been thrown by the join optimization executor");
                }
            }
        }
    }


}
