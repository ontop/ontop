package it.unibz.inf.ontop.iq.proposal.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;

import java.util.*;
import java.util.stream.Stream;


public class NodeTrackerImpl implements NodeTracker {

    private static class NodeUpdateImpl<N extends QueryNode> implements NodeUpdate<N> {
        private final Optional<N> newNode;
        private final Optional<QueryNode> replacingChild;
        private final Optional<QueryNode> nextSibling;
        private final Optional<QueryNode> closestAncestor;

        private NodeUpdateImpl(N newNode) {
            this.newNode = Optional.of(newNode);
            this.replacingChild = Optional.empty();
            this.nextSibling = Optional.empty();
            this.closestAncestor = Optional.empty();
        }

        private NodeUpdateImpl(Optional<QueryNode> optionalReplacingChild) {
            if (!optionalReplacingChild.isPresent()) {
                throw new IllegalArgumentException("The replacing child must be given (fake optional)");
            }

            this.newNode = Optional.empty();

            this.replacingChild = optionalReplacingChild;
            this.nextSibling = Optional.empty();
            this.closestAncestor = Optional.empty();
        }

        private NodeUpdateImpl(Optional<QueryNode> nextSibling,
                               Optional<QueryNode> closestAncestor) {
            this.newNode = Optional.empty();
            this.replacingChild = Optional.empty();
            this.nextSibling = nextSibling;
            this.closestAncestor = closestAncestor;
        }

        @Override
        public Optional<N> getNewNode() {
            return newNode;
        }

        @Override
        public Optional<QueryNode> getReplacingChild() {
            return replacingChild;
        }

        @Override
        public Optional<QueryNode> getNewNodeOrReplacingChild() {
            if (newNode.isPresent()) {
                return (Optional<QueryNode>) newNode;
            }
            return replacingChild;
        }

        @Override
        public Optional<QueryNode> getOptionalNextSibling(IntermediateQuery query) {
            return getNewNodeOrReplacingChild()
                    .map(query::getNextSibling)
                    .orElse(nextSibling);
        }

        @Override
        public Optional<QueryNode> getOptionalClosestAncestor(IntermediateQuery query) {
            return getNewNodeOrReplacingChild()
                    .map(query::getParent)
                    .orElse(closestAncestor);
        }

    }

    // Original parent -> replacing child
    private final Map<QueryNode, QueryNode> childReplacement;
    private final BiMap<QueryNode, QueryNode> nodeUpdate;
    /**
     * Original node -> closest ancestor
     * (this was holding at some point in time).
     *
     * Only concerns dropped nodes
     *
     * TODO: should we only consider the parent (it is ok if the latter has been dropped)?
     *
     */
    private final Map<QueryNode, QueryNode> closestAncestorMap;

    /**
     * Original ancestor -> next sibling
     * (this was holding at some point in time).
     *
     * Only concerns dropped nodes
     */
    private final Map<QueryNode, QueryNode> nextSiblingMap;

    /**
     * Nodes which removal has been declared
     */
    private final Set<QueryNode> droppedNodes;

    public NodeTrackerImpl() {
        childReplacement = new HashMap<>();
        nodeUpdate = HashBiMap.create();
        closestAncestorMap = new HashMap<>();
        droppedNodes = new HashSet<>();
        nextSiblingMap = new HashMap<>();
    }

    /**
     * TODO: handle the removal declaration of the other children. Make sure this method is called before the removal
     */
    @Override
    public void recordUpcomingReplacementByChild(IntermediateQuery query, QueryNode ancestorNode,
                                                 QueryNode replacingDescendantNode) {
        if (nodeUpdate.containsKey(ancestorNode)) {
            throw new IllegalArgumentException("This ancestor node (" + ancestorNode
                    + ") has already been updated");
        }

        childReplacement.put(ancestorNode, replacingDescendantNode);
        /**
         * The child must not become the ancestor of the other children
         * So the parent of the ancestor becomes the closest ancestor for these nodes (that will removed by the way)
         *
         */
        query.getParent(ancestorNode)
                .ifPresent(p -> closestAncestorMap.put(ancestorNode, p));
    }

    @Override
    public void recordReplacement(QueryNode formerNode, QueryNode newNode) {
        if (childReplacement.containsKey(formerNode)) {
            throw new IllegalArgumentException("This ancestor node (" + formerNode
                    + ") has already been replaced by a child");
        }
        else if (droppedNodes.contains(formerNode)) {
            throw new IllegalArgumentException("This ancestor node (" + formerNode
                    + ") has already been declared as useless");
        }
        nodeUpdate.put(formerNode, newNode);
    }

    @Override
    public void recordUpcomingRemoval(IntermediateQuery query, QueryNode subTreeRoot) {
        Stream<QueryNode> subTreeNodeStream = Stream.concat(Stream.of(subTreeRoot),
                query.getSubTreeNodesInTopDownOrder(subTreeRoot).stream());

        /**
         * TODO: explain
         */
        Optional<QueryNode> optionalNextExtendedSibling = query.getNextSibling(subTreeRoot);
        Optional<QueryNode> optionalRemainingAncestor = query.getParent(subTreeRoot);

        /**
         * TODO: make the distinction between next sibling and next extended sibling
         */
        subTreeNodeStream
                .forEach(n -> recordUpcomingRemovalIndividualNode(n, optionalNextExtendedSibling, optionalRemainingAncestor));
    }

    private void recordUpcomingRemovalIndividualNode(QueryNode node, Optional<QueryNode> optionalNextSibling,
                                                     Optional<QueryNode> optionalClosestAncestor) {
        if (childReplacement.containsKey(node)) {
            throw new IllegalArgumentException("This ancestor node (" + node
                    + ") has already been replaced by a child");
        }
        else if (nodeUpdate.containsKey(node)) {
            throw new IllegalArgumentException("This ancestor node (" + node
                    + ") has already been updated");
        }
        droppedNodes.add(node);
        optionalNextSibling.ifPresent(s -> nextSiblingMap.put(node, s));

        // NB: if the closest ancestor is not present, an EmptyQueryException is expected to be generated soon
        optionalClosestAncestor
                .ifPresent(a -> closestAncestorMap.put(node, a));
    }


    @Override
    public <N extends QueryNode> NodeUpdate<N> getUpdate(IntermediateQuery query, N node) {
        if (droppedNodes.contains(node)) {

            // TODO: distinguish the next sibling from the extended one
            Optional<QueryNode> nextExtendedSibling = getNextSibling(node)
                    .map(Optional::of)
                    .orElseGet(() -> getNextExtendedSibling(node));

            return new NodeUpdateImpl<>(nextExtendedSibling, getClosestAncestor(query, node));
        }
        else if (nodeUpdate.containsKey(node)) {
            return getUpdate(query, (N) nodeUpdate.get(node));
        }
        else if (childReplacement.containsKey(node)) {
            QueryNode initialReplacingChild = childReplacement.get(node);
            NodeUpdate<QueryNode> update = getUpdate(query, initialReplacingChild);

            Optional<QueryNode> optionalNewReplacingChild = update.getNewNodeOrReplacingChild();
            if (optionalNewReplacingChild.isPresent()) {
                return new NodeUpdateImpl<>(optionalNewReplacingChild);
            }
            else {
                return new NodeUpdateImpl<>(getNextSibling(node), getClosestAncestor(query, initialReplacingChild));
            }
        }
        else {
            return new NodeUpdateImpl<>(node);
        }
    }

    private Optional<QueryNode> getClosestAncestor(IntermediateQuery query, QueryNode node) {

        if (closestAncestorMap.containsKey(node)) {
            QueryNode initialClosestAncestor = closestAncestorMap.get(node);

            NodeUpdate<QueryNode> ancestorUpdate = getUpdate(query, initialClosestAncestor);
            Optional<QueryNode> optionalNewAncestor = ancestorUpdate.getNewNode();
            if (optionalNewAncestor.isPresent()) {
                return optionalNewAncestor;
            }
            return ancestorUpdate.getOptionalClosestAncestor(query);
        }
        else if (nodeUpdate.containsKey(node)) {
            return getClosestAncestor(query, nodeUpdate.get(node));
        }
        /**
         * When a node is replaced by its child, its closest ancestor is expected to be be declared.
         *
         * If the node is still present in the tree, no closest ancestor is returned by the tracker.
         */
        else {
            return Optional.empty();
        }
    }

    private Optional<QueryNode> getNextSibling(QueryNode node) {
        if (!droppedNodes.contains(node)) {
            throw new IllegalArgumentException("Only deals with dropped nodes");
        }

        Optional<QueryNode> optionalInitialNextSibling = Optional.ofNullable(nextSiblingMap.get(node));

        if (!optionalInitialNextSibling.isPresent()) {
            return Optional.empty();
        }

        QueryNode initialNextSibling = optionalInitialNextSibling.get();


        Optional<QueryNode> optionalDirectNextSibling = getCurrentReplacingChild(initialNextSibling);

        if (optionalDirectNextSibling.isPresent()) {
            return optionalDirectNextSibling;
        }
        /**
         * When a node is removed, its next sibling becomes the next sibling of its previous sibling
         *
         * Recursive
         *
         */
        else {
            return getNextSibling(initialNextSibling);
        }
    }

    /**
     *
     * Here the next extended next sibling IS NOT the (standard) next sibling.
     *
     * Said differently, it assumes the node has no next sibling (won't look for it).
     *
     */
    private Optional<QueryNode> getNextExtendedSibling(QueryNode node) {
        if (!droppedNodes.contains(node)) {
            throw new IllegalArgumentException("Only deals with dropped nodes");
        }

        // Non-final
        Optional<QueryNode> lastRemovedAncestor = Optional.empty();
        Optional<QueryNode> optionalCurrentAncestor = Optional.ofNullable(closestAncestorMap.get(node));

        while (optionalCurrentAncestor.isPresent()) {
            // NB: Only the updated node can be dropped
            QueryNode updatedAncestor = getUpdatedNode(optionalCurrentAncestor.get());
            if (droppedNodes.contains(updatedAncestor)) {
                lastRemovedAncestor = Optional.of(updatedAncestor);
            }
            optionalCurrentAncestor = Optional.ofNullable(closestAncestorMap.get(updatedAncestor));
        }

        return lastRemovedAncestor
                // Looks first for the next sibling
                .flatMap(n -> Optional.ofNullable(nextSiblingMap.get(n))
                        .map(Optional::of)
                        // Otherwise look for the extended sibling
                        .orElseGet(() -> getNextExtendedSibling(n)));
    }


    /**
     * TODO: does it make sense?
     */
    private Optional<QueryNode> getCurrentReplacingChild(QueryNode node) {
        if (droppedNodes.contains(node)) {
            return Optional.empty();
        }
        else if (nodeUpdate.containsKey(node)) {
            return getCurrentReplacingChild(nodeUpdate.get(node));
        }
        else if (childReplacement.containsKey(node)) {
            return getCurrentReplacingChild(childReplacement.get(node));
        }
        else {
            return Optional.of(node);
        }
    }

    /**
     * Only consider standard replacement
     */
    private QueryNode getUpdatedNode(QueryNode node) {
        if (nodeUpdate.containsKey(node)) {
            // Recursive
            return getUpdatedNode(nodeUpdate.get(node));
        }
        else {
            return node;
        }
    }
}
