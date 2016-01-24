package org.semanticweb.ontop.owlrefplatform.core.optimization;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * TODO: explain
 */
public abstract class TopDownOptimizer implements IntermediateQueryOptimizer{

    protected Optional<QueryNode> getNaturalNextNode(IntermediateQuery currentQuery, QueryNode freshlyExploredNode) {
        Optional<QueryNode> optionalFirstChild = currentQuery.getFirstChild(freshlyExploredNode);

        return optionalFirstChild.isPresent()
                ? optionalFirstChild
                : getNextNodeSameOrUpperLevel(currentQuery, freshlyExploredNode);
    }

    /**
     * Assumes a top-down exploration.
     *
     * DOES NOT LOOK DOWN (the sub-tree of the initialAlreadyExploredNode is supposed to have already been explored)
     *
     */
    protected Optional<QueryNode> getNextNodeSameOrUpperLevel(final IntermediateQuery query, final QueryNode initialAlreadyExploredNode) {
        Optional<QueryNode> optionalAlreadyExploredNode = Optional.of(initialAlreadyExploredNode);

        while(optionalAlreadyExploredNode.isPresent()) {
            QueryNode currentAlreadyExploredNode = optionalAlreadyExploredNode.get();

            Optional<QueryNode> optionalNextSibling = query.getNextSibling(currentAlreadyExploredNode);

            if (optionalNextSibling.isPresent()) {
                return optionalNextSibling;
            }
            else {
                optionalAlreadyExploredNode = query.getParent(currentAlreadyExploredNode);
            }
        }
        return Optional.empty();
    }
}
