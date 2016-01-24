package org.semanticweb.ontop.owlrefplatform.core.optimization;


import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;

import java.util.Optional;

public class QueryNodeNavigationTools {

    /**
     * Next node and updated query
     */
    public static class NextNodeAndQuery {
        private final Optional<QueryNode> optionalNextNode;
        private final IntermediateQuery nextQuery;

        public NextNodeAndQuery(Optional<QueryNode> optionalNextNode, IntermediateQuery nextQuery) {
            this.optionalNextNode = optionalNextNode;
            this.nextQuery = nextQuery;
        }

        public Optional<QueryNode> getOptionalNextNode() {
            return optionalNextNode;
        }

        public IntermediateQuery getNextQuery() {
            return nextQuery;
        }
    }

    /**
     * Updated node and query
     */
    public static class UpdatedNodeAndQuery<N extends QueryNode> {
        private final N updatedQuery;
        private final IntermediateQuery nextQuery;

        public UpdatedNodeAndQuery(N updatedNode, IntermediateQuery nextQuery) {
            this.updatedQuery = updatedNode;
            this.nextQuery = nextQuery;
        }

        public N getUpdatedNode() {
            return updatedQuery;
        }

        public IntermediateQuery getNextQuery() {
            return nextQuery;
        }
    }


    /**
     * Depth-first exploration
     */
    public static Optional<QueryNode> getDepthFirstNextNode(IntermediateQuery query, QueryNode currentNode) {
        return getDepthFirstNextNode(query, currentNode, false);
    }

    /**
     * Finds the next node to visit in a new intermediate query
     */
    public static NextNodeAndQuery getNextNodeAndQuery(NodeCentricOptimizationResults<? extends QueryNode> results) {
        IntermediateQuery query = results.getResultingQuery();

        /**
         * If there is still a node at center: gets the next one of it
         */
        Optional<? extends QueryNode> optionalNewNode = results.getOptionalNewNode();
        if (optionalNewNode.isPresent()) {
            Optional<QueryNode> optionalNextNode = getDepthFirstNextNode(query, optionalNewNode.get());

            return new NextNodeAndQuery(optionalNextNode, query);
        }

        /**
         * Otherwise, if there is a replacing child: returns it
         */
        Optional<QueryNode> optionalReplacingChild = results.getOptionalReplacingChild();
        if (optionalReplacingChild.isPresent()) {
            return new NextNodeAndQuery(optionalReplacingChild, query);
        }

        /**
         * Otherwise, if there is a next sibling: returns it
         */
        Optional<QueryNode> optionalNextSibling = results.getOptionalNextSibling();
        if (optionalNextSibling.isPresent()) {
            return new NextNodeAndQuery(optionalNextSibling, query);
        }

        /**
         * Otherwise, looks for the next node of the closest ancestor (already visited)
         */
        Optional<QueryNode> optionalClosestAncestor = results.getOptionalClosestAncestor();
        if (optionalClosestAncestor.isPresent()) {
            Optional<QueryNode> optionalNextNode = getDepthFirstNextNode(query, optionalClosestAncestor.get(), true);
            return new NextNodeAndQuery(optionalNextNode, query);
        }
        /**
         * Nothing else to explore
         */
        else {
            return new NextNodeAndQuery(Optional.<QueryNode>empty(), query);
        }
    }

    private static Optional<QueryNode> getDepthFirstNextNode(IntermediateQuery query, QueryNode currentNode,
                                                            boolean alreadyExploredSubTree) {

        /**
         * First choice: first child
         */
        if (!alreadyExploredSubTree) {
            Optional<QueryNode> optionalFirstChild = query.getFirstChild(currentNode);

            if (optionalFirstChild.isPresent()) {
                return optionalFirstChild;
            }
        }

        /**
         * Second choice: next sibling
         */
        Optional<QueryNode> optionalNextSibling = query.getNextSibling(currentNode);
        if (optionalNextSibling.isPresent()) {
            return optionalNextSibling;
        }

        /**
         * Otherwise, tries the closest next sibling of an ancestor (recursive call)
         */
        Optional<QueryNode> optionalParent = query.getParent(currentNode);
        if (optionalParent.isPresent()) {
            // Recursive call
            return getDepthFirstNextNode(query, optionalParent.get(), true);
        }

        /**
         * No more node to explore
         */
        return Optional.empty();
    }

}
