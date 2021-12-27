package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: explain
 *
 * Mutable
 *
 */
public class DefaultTree implements QueryTree {

    private final TreeNode rootNode;
    private final Map<QueryNode, TreeNode> nodeIndex;
    private final Map<TreeNode, ChildrenRelation> childrenIndex;
    private final Map<TreeNode, TreeNode> parentIndex;
    private final Set<TrueNode> trueNodes;
    private final Set<IntensionalDataNode> intensionalNodes;
    private UUID versionNumber;


    protected DefaultTree(QueryNode rootQueryNode) {
        nodeIndex = new HashMap<>();
        childrenIndex = new HashMap<>();
        parentIndex = new HashMap<>();
        trueNodes = new HashSet<>();
        intensionalNodes = new HashSet<>();

        // Adds the root node
        rootNode = new TreeNode(rootQueryNode);
        insertNodeIntoIndex(rootQueryNode, rootNode);
        childrenIndex.put(rootNode, createChildrenRelation(rootNode));
        // No parent

        versionNumber = UUID.randomUUID();
    }

    private DefaultTree(TreeNode rootNode,
                        Map<QueryNode, TreeNode> nodeIndex,
                        Map<TreeNode, ChildrenRelation> childrenIndex,
                        Map<TreeNode, TreeNode> parentIndex,
                        Set<TrueNode> trueNodes,
                        Set<IntensionalDataNode> intensionalNodes,
                        UUID versionNumber) {
        this.rootNode = rootNode;
        this.nodeIndex = nodeIndex;
        this.childrenIndex = childrenIndex;
        this.parentIndex = parentIndex;
        this.trueNodes = trueNodes;
        this.intensionalNodes = intensionalNodes;
        this.versionNumber = versionNumber;
    }

    @Override
    public QueryNode getRootNode() {
        return rootNode.getQueryNode();
    }

    @Override
    public void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException {
        TreeNode parentNode = accessTreeNode(parentQueryNode);

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
    private void createNewNode(QueryNode childQueryNode, TreeNode parentNode,
                               Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException {
        TreeNode childNode = new TreeNode(childQueryNode);
        insertNodeIntoIndex(childQueryNode, childNode);

        childrenIndex.put(childNode, createChildrenRelation(childNode));

        parentIndex.put(childNode, parentNode);
        accessChildrenRelation(parentNode).addChild(childNode, optionalPosition);
    }

    private static ChildrenRelation createChildrenRelation(TreeNode parentTreeNode) {
        if (parentTreeNode.getQueryNode() instanceof BinaryOrderedOperatorNode) {
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
            for (TreeNode treeNode : childrenRelation.getChildren()) {
                builder.add(treeNode.getQueryNode());
            }
            return builder.build();
        }
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() {
        Queue<TreeNode> nodesToExplore = new LinkedList<>();
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        nodesToExplore.add(rootNode);
        builder.add(rootNode.getQueryNode());

        while (!nodesToExplore.isEmpty()) {
            TreeNode node = nodesToExplore.poll();
            for (TreeNode childNode : accessChildrenRelation(node).getChildren()) {
                nodesToExplore.add(childNode);
                builder.add(childNode.getQueryNode());
            }
        }
        return builder.build();
    }

    @Override
    public QueryTree createSnapshot() {
        Map<QueryNode, TreeNode> newNodeIndex = nodeIndex.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().cloneShallowly()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        Map<TreeNode, ChildrenRelation> newChildrenIndex = childrenIndex.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(
                        e.getKey().findNewTreeNode(newNodeIndex),
                        e.getValue().clone(newNodeIndex)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        Map<TreeNode, TreeNode> newParentIndex = parentIndex.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().findNewTreeNode(newNodeIndex),
                        e.getValue().findNewTreeNode(newNodeIndex)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        return new DefaultTree(newNodeIndex.get(rootNode.getQueryNode()), newNodeIndex, newChildrenIndex,
                newParentIndex, new HashSet<>(trueNodes), new HashSet<>(intensionalNodes), versionNumber);
    }

    private void updateVersionNumber() {
        versionNumber = UUID.randomUUID();
    }

    private TreeNode accessTreeNode(QueryNode node) {
        TreeNode treeNode = nodeIndex.get(node);
        if (treeNode == null) {
            throw new IllegalArgumentException("The given query node is not in the tree");
        }
        return treeNode;
    }

    private ChildrenRelation accessChildrenRelation(TreeNode node) {
        ChildrenRelation relation = childrenIndex.get(node);
        if (relation == null) {
            throw new RuntimeException("Internal error: the tree node does not have a children relation.");
        }
        return relation;
    }


    /**
     * Low-low-level
     */
    private void insertNodeIntoIndex(QueryNode queryNode, TreeNode treeNode) {
        nodeIndex.put(queryNode, treeNode);
        if (queryNode instanceof TrueNode){
           trueNodes.add((TrueNode) queryNode);
        }
        else if (queryNode instanceof IntensionalDataNode){
            intensionalNodes.add((IntensionalDataNode) queryNode);
        }
        updateVersionNumber();
    }
}
