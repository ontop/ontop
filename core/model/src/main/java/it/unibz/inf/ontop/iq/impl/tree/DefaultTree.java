package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.*;

import java.util.*;

/**
 * TODO: explain
 *
 * Mutable
 *
 */
public class DefaultTree implements QueryTree {

    private final QueryNode rootNode;
    private final Map<QueryNode, QueryNode> nodeIndex;
    private final Map<QueryNode, ChildrenRelation> childrenIndex;

    protected DefaultTree(QueryNode rootQueryNode) {
        nodeIndex = new HashMap<>();
        childrenIndex = new HashMap<>();

        // Adds the root node
        rootNode = rootQueryNode;
        nodeIndex.put(rootQueryNode, rootNode);
        childrenIndex.put(rootNode, createChildrenRelation(rootNode));
        // No parent

    }

    @Override
    public QueryNode getRootNode() {
        return rootNode;
    }

    @Override
    public void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException {
        QueryNode parentNode = accessTreeNode(parentQueryNode);

        if (nodeIndex.containsKey(childQueryNode)) {
            throw new IllegalTreeUpdateException("Node " + childQueryNode + " already in the graph");
        }
        /**
         * New node
         */
        else {
            createNewNode(childQueryNode, parentNode, optionalPosition);
        }
    }

    /**
     * Low-level
     */
    private void createNewNode(QueryNode childQueryNode, QueryNode parentNode,
                               Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException {
        QueryNode childNode = childQueryNode;
        nodeIndex.put(childQueryNode, childNode);

        childrenIndex.put(childNode, createChildrenRelation(childNode));

        accessChildrenRelation(parentNode).addChild(childNode, optionalPosition);
    }

    private static ChildrenRelation createChildrenRelation(QueryNode parentTreeNode) {
        if (parentTreeNode instanceof BinaryOrderedOperatorNode) {
            return new BinaryChildrenRelation(parentTreeNode);
        }
        else {
            return new StandardChildrenRelation(parentTreeNode);
        }
    }


    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        ChildrenRelation childrenRelation = accessChildrenRelation(accessTreeNode(node));
        if (childrenRelation == null) {
            return ImmutableList.of();
        }
        else {
            ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
            for (QueryNode treeNode : childrenRelation.getChildren()) {
                builder.add(treeNode);
            }
            return builder.build();
        }
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() {
        Queue<QueryNode> nodesToExplore = new LinkedList<>();
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        nodesToExplore.add(rootNode);
        builder.add(rootNode);

        while (!nodesToExplore.isEmpty()) {
            QueryNode node = nodesToExplore.poll();
            for (QueryNode childNode : accessChildrenRelation(node).getChildren()) {
                nodesToExplore.add(childNode);
                builder.add(childNode);
            }
        }
        return builder.build();
    }

    private QueryNode accessTreeNode(QueryNode node) {
        QueryNode treeNode = nodeIndex.get(node);
        if (treeNode == null) {
            throw new IllegalArgumentException("The given query node is not in the tree");
        }
        return treeNode;
    }

    private ChildrenRelation accessChildrenRelation(QueryNode node) {
        ChildrenRelation relation = childrenIndex.get(node);
        if (relation == null) {
            throw new RuntimeException("Internal error: the tree node does not have a children relation.");
        }
        return relation;
    }


}
