package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;

import java.util.*;

/**
 * Basic implementation based on a JGrapht DAG.
 *
 * TODO: debug it
 *
 */
public class JgraphtQueryTreeComponent implements QueryTreeComponent {

    private static final Optional<ArgumentPosition> NO_POSITION = Optional.absent();
    private static final Optional<ArgumentPosition> LEFT_POSITION = Optional.of(ArgumentPosition.LEFT);
    private static final Optional<ArgumentPosition> RIGHT_POSITION = Optional.of(ArgumentPosition.RIGHT);

    /**
     * TODO: explain
     */
    public static class LabeledEdge extends DefaultEdge implements Comparable<LabeledEdge> {

        private final Optional<ArgumentPosition> optionalPosition;

        public LabeledEdge() {
            this.optionalPosition = Optional.absent();
        }

        public LabeledEdge(Optional<ArgumentPosition> optionalPosition) {
            this.optionalPosition = optionalPosition;
        }

        public LabeledEdge(ArgumentPosition position) {
            this.optionalPosition = Optional.of(position);
        }

        public Optional<ArgumentPosition> getOptionalPosition() {
            return optionalPosition;
        }

        @Override
        public int compareTo(LabeledEdge o) {
            Optional<ArgumentPosition> otherOptionalPosition = o.getOptionalPosition();

            if (optionalPosition.isPresent()) {
                if (otherOptionalPosition.isPresent()) {
                    return optionalPosition.get().compareTo(otherOptionalPosition.get());
                }
                else {
                    return -1;
                }
            }
            else if (otherOptionalPosition.isPresent()) {
                return 1;
            }

            return 0;
         }
    }


    /**
     * TODO: explain.
     *
     * Implementation detail: this object must NOT BE SHARED with the other classes.
     */
    private final DirectedAcyclicGraph<QueryNode, LabeledEdge> queryDAG;

    /**
     * MAKE SURE it remains the "root" of the tree.
     * MAY BE NULL!
     *
     * TODO: mark it as Nullable.
     */
    private ConstructionNode rootConstructionNode;

    /**
     * Cached value (non final). MAY BE NULL
     *
     * * TODO: mark it as Nullable.
     */
    private ImmutableList<QueryNode> bottomUpOrderedNodes;


    protected JgraphtQueryTreeComponent(DirectedAcyclicGraph<QueryNode, LabeledEdge> queryDAG)
            throws IllegalTreeException {
        this.queryDAG = queryDAG;
        /**
         * Cache attributes.
         * May throw an IllegalTreeException during their computation.
         *
         */
        this.bottomUpOrderedNodes = null;
        this.rootConstructionNode = null;
        computeNodeTopologyCache();
    }

    @Override
    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node) {
        return getSubNodesOf(queryDAG, node);
    }

    @Override
    public ConstructionNode getRootConstructionNode() throws IllegalTreeException {
        if (rootConstructionNode == null) {
            computeNodeTopologyCache();
        }
        return rootConstructionNode;
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() throws IllegalTreeException {

        /**
         * Computes the list if not cached
         */
        if (bottomUpOrderedNodes == null) {
            computeNodeTopologyCache();
        }

        return bottomUpOrderedNodes;
    }

    @Override
    public boolean contains(QueryNode node) {
        return queryDAG.containsVertex(node);
    }


    /**
     * TODO: explain
     */
    @Override
    public void replaceNode(QueryNode previousNode, QueryNode replacingNode) {
        resetNodeTopologyCache();

        queryDAG.addVertex(replacingNode);
        try {
            for (LabeledEdge incomingEdge : queryDAG.incomingEdgesOf(previousNode)) {
                QueryNode child = queryDAG.getEdgeSource(incomingEdge);
                queryDAG.addDagEdge(child, replacingNode, new LabeledEdge(incomingEdge.getOptionalPosition()));
            }

            for (LabeledEdge outgoingEdge : queryDAG.outgoingEdgesOf(previousNode)) {
                QueryNode parent = queryDAG.getEdgeTarget(outgoingEdge);
                queryDAG.addDagEdge(replacingNode, parent, new LabeledEdge(outgoingEdge.getOptionalPosition()));
            }

        } catch (DirectedAcyclicGraph.CycleFoundException e) {
            throw new RuntimeException("BUG: " + e.getLocalizedMessage());
        }
        queryDAG.removeVertex(previousNode);
    }

    /**
     * TODO: explain
     * TODO: replace this recursive implementation but iterative one
     * Low-level. Tail recursive.
     */
    @Override
    public void addSubTree(IntermediateQuery subQuery, QueryNode parentNode) {
        for (QueryNode childNode : subQuery.getCurrentSubNodesOf(parentNode)) {
            queryDAG.addVertex(childNode);
            try {
                Optional<ArgumentPosition> optionalPosition = subQuery.getOptionalPosition(parentNode, childNode);
                queryDAG.addDagEdge(childNode, parentNode, new LabeledEdge(optionalPosition));
            } catch (DirectedAcyclicGraph.CycleFoundException e) {
                throw new RuntimeException("BUG (internal error)" + e.getLocalizedMessage());
            }
            // Recursive call
            addSubTree(subQuery, childNode);
        }
    }

    @Override
    public void setChildrenNodes(QueryNode parentNode, List<QueryNode> allChildrenNodes) throws IllegalTreeException {
        boolean isAsymmetric = (parentNode instanceof BinaryAsymmetricOperatorNode);

        if (isAsymmetric && allChildrenNodes.size() != 2) {
            throw new IllegalTreeException("A BinaryAsymmetricOperatorNode requires two children, " +
                    "not " + allChildrenNodes);
        }

        Set<QueryNode> proposedSubNodesToConsider = new HashSet<>(allChildrenNodes);

        /**
         * Existing sub-nodes: keep or remove
         */
        Set<LabeledEdge> incomingEdges = queryDAG.incomingEdgesOf(parentNode);
        for (LabeledEdge subNodeEdge : incomingEdges) {
            QueryNode subNode = queryDAG.getEdgeSource(subNodeEdge);
            // Kept
            if (proposedSubNodesToConsider.contains(subNode)) {
                proposedSubNodesToConsider.remove(subNode);
            }
            // Removed
            else {
                removeSubTree(subNode);
            }
        }

        /**
         * New sub-nodes: added to the DAG
         */
        int i = 0;
        for (QueryNode newSubNode : proposedSubNodesToConsider) {
            if (!queryDAG.containsVertex(newSubNode)) {
                queryDAG.addVertex(newSubNode);
            }
            LabeledEdge edge;
            if (isAsymmetric) {
                if (i == 0) {
                    edge = new LabeledEdge(LEFT_POSITION);
                }
                else {
                    edge = new LabeledEdge(RIGHT_POSITION);
                }
            }
            else {
                edge = new LabeledEdge(NO_POSITION);
            }
            try {
                queryDAG.addDagEdge(parentNode, newSubNode, edge);
            } catch (DirectedAcyclicGraph.CycleFoundException ex) {
                // Inconsistent proposal (should not introduce a cycle in the DAG) --> throw an exception.
                // TODO: return a non- RuntimeException.
                throw new IllegalTreeException(ex.getMessage());
            }
            i++;
        }
    }

    /**
     * Removes all the nodes of a sub-tree,
     * all the edges between them and WITH THE REST OF TREE.
     *
     * Recursive
     */
    private void removeSubTree(QueryNode subTreeRoot) {
        for (LabeledEdge subNodeEdge : queryDAG.incomingEdgesOf(subTreeRoot)) {
            QueryNode childNode = queryDAG.getEdgeSource(subNodeEdge);
            /**
             * Recursive call.
             * Removing this edge is the responsibility of the child node.
             */
            removeSubTree(childNode);
        }

        for (LabeledEdge parentEdge : queryDAG.outgoingEdgesOf(subTreeRoot)) {
            queryDAG.removeEdge(parentEdge);
        }

        queryDAG.removeVertex(subTreeRoot);
    }

    /**
     * The root is EXCLUDED
     */
    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode topNode) {

        ImmutableList.Builder<QueryNode> nodeBuilder = ImmutableList.builder();

        Queue<QueryNode> nodesToVisit = new LinkedList<>(getCurrentSubNodesOf(topNode));
        while(!nodesToVisit.isEmpty()) {
            QueryNode node = nodesToVisit.poll();
            nodeBuilder.add(node);
            nodesToVisit.addAll(getCurrentSubNodesOf(node));
        }
        return nodeBuilder.build();
    }

    @Override
    public Optional<ArgumentPosition> getOptionalPosition(QueryNode parentNode, QueryNode childNode) {
        LabeledEdge edge = queryDAG.getEdge(childNode, parentNode);
        if (edge == null)
            return Optional.absent();

        return edge.getOptionalPosition();
    }

    @Override
    public ImmutableList<QueryNode> getAncestors(final QueryNode descendantNode) throws IllegalTreeException {
        ImmutableList.Builder<QueryNode> ancestorBuilder = ImmutableList.builder();

        QueryNode parentNode;
        Set<LabeledEdge> toParentEdges = queryDAG.outgoingEdgesOf(descendantNode);
        while (!toParentEdges.isEmpty()) {
            if (toParentEdges.size() > 1)
                throw new IllegalTreeException("A tree node must not have more than one parent!");

            parentNode = queryDAG.getEdgeTarget(toParentEdges.iterator().next());
            ancestorBuilder.add(parentNode);
            toParentEdges = queryDAG.outgoingEdgesOf(parentNode);
        }

        return ancestorBuilder.build();
    }

    @Override
    public Optional<QueryNode> getParent(QueryNode node) throws IllegalTreeException {
        Set<LabeledEdge> toParentEdges = queryDAG.outgoingEdgesOf(node);

        switch (toParentEdges.size()) {
            case 0:
                return Optional.absent();
            case 1:
                return Optional.of(queryDAG.getEdgeTarget(toParentEdges.iterator().next()));
            default:
                throw new IllegalTreeException("More than one parent found!");
        }
    }

    @Override
    public void removeOrReplaceNodeByUniqueChildren(QueryNode node) throws IllegalTreeUpdateException {
        ImmutableList<QueryNode> children = getCurrentSubNodesOf(node);
        int nbChildren = children.size();
        switch(nbChildren) {
            case 0:
                removeSubTree(node);
                return;
            case 1:
                QueryNode child = children.get(0);
                replaceNodeByUniqueChildren(node, child);
                return;
            default:
                throw new IllegalTreeUpdateException(node.toString() + " has more child. Cannot be replaced");
        }
    }

    @Override
    public void replaceNodesByOneNode(ImmutableList<QueryNode> queryNodes, QueryNode replacingNode, boolean isNewNode) throws IllegalTreeUpdateException {
        throw new RuntimeException("TODO: implement replaceNodesByOneNode()");
    }

    @Override
    public void removeNodeAndItsSubTree(QueryNode node) {
        throw new RuntimeException("TODO: implement removeNodeAndItsSubTree()");
    }

    private void replaceNodeByUniqueChildren(QueryNode nodeToReplace, QueryNode replacingChild) {
        ImmutableSet<LabeledEdge> toParentEdges = ImmutableSet.copyOf(queryDAG.outgoingEdgesOf(nodeToReplace));

        /**
         * Links to parents
         */
        for (LabeledEdge outgoingEdge : toParentEdges) {
            QueryNode parent = queryDAG.getEdgeTarget(outgoingEdge);
            try {
                queryDAG.addDagEdge(replacingChild, parent, new LabeledEdge(outgoingEdge.getOptionalPosition()));
            } catch (DirectedAcyclicGraph.CycleFoundException e) {
                throw new RuntimeException(e.getMessage());
            }
            queryDAG.removeEdge(outgoingEdge);
        }
    }


    /**
     * TODO: describe
     */
    private void computeNodeTopologyCache() throws IllegalTreeException {
        bottomUpOrderedNodes = extractBottomUpOrderedNodes(queryDAG);
        rootConstructionNode = extractRootProjectionNode(bottomUpOrderedNodes);
    }

    /**
     * TODO: describe
     */
    private void resetNodeTopologyCache() {
        bottomUpOrderedNodes = null;
        rootConstructionNode = null;
    }

    /**
     * TODO: describe
     */
    private static ImmutableList<QueryNode> extractBottomUpOrderedNodes(
            DirectedAcyclicGraph<QueryNode, LabeledEdge> queryDAG) {
        TopologicalOrderIterator<QueryNode, LabeledEdge> it =
                new TopologicalOrderIterator<>(queryDAG);

        return ImmutableList.copyOf(it);
    }

    /**
     * TODO: describe
     */
    private static ConstructionNode extractRootProjectionNode(ImmutableList<QueryNode> topDownOrderedNodes)
            throws IllegalTreeException {
        if (topDownOrderedNodes.isEmpty()) {
            throw new IllegalTreeException("Empty DAG!");
        }

        QueryNode rootNode = topDownOrderedNodes.get(topDownOrderedNodes.size() - 1);
        if (!(rootNode instanceof ConstructionNode)) {
            throw new IllegalTreeException("The root node is not a ConstructionNode: " + rootNode);
        }

        return (ConstructionNode) rootNode;
    }

    @Override
    public String toString() {
        return queryDAG.toString();
    }


    /**
     * Edges are directed from the child to the parent.
     */
    protected static ImmutableList<QueryNode> getSubNodesOf(DirectedAcyclicGraph<QueryNode, LabeledEdge> queryDAG,
                                                            QueryNode node) {

        Collection<LabeledEdge> incomingEdges = sortEdgesIfNecessary(queryDAG.incomingEdgesOf(node), node);
        ImmutableList.Builder<QueryNode> nodeListBuilder = ImmutableList.builder();
        for (LabeledEdge edge : incomingEdges) {
            nodeListBuilder.add(queryDAG.getEdgeSource(edge));
        }

        return nodeListBuilder.build();
    }

    private static Collection<LabeledEdge> sortEdgesIfNecessary(Set<LabeledEdge> edges, QueryNode parentNode) {
        if (parentNode instanceof BinaryAsymmetricOperatorNode) {
            List<LabeledEdge> edgeList = new ArrayList<>(edges);
            Collections.sort(edgeList);
            return edgeList;
        }
        /**
         * By default, does nothing
         */
        return edges;
    }

}
