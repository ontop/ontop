package org.semanticweb.ontop.pivotalrepr.impl.tree;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * TODO: explain
 *
 * Mutable
 *
 */
public class DefaultTree implements QueryTree {

    /**
     * Final but MUTABLE attributes
     */
    private final TreeNode rootNode;
    private final Map<QueryNode, TreeNode> nodeIndex;
    private final Map<TreeNode, ChildrenRelation> childrenIndex;
    private final Map<TreeNode, TreeNode> parentIndex;


    protected DefaultTree(ConstructionNode rootQueryNode) {
        rootNode = new TreeNode(rootQueryNode);
        nodeIndex = new HashMap<>();
        nodeIndex.put(rootQueryNode, rootNode);

        childrenIndex = new HashMap<>();
        parentIndex = new HashMap<>();
    }

    @Override
    public ConstructionNode getRootNode() {
        return (ConstructionNode) rootNode.getQueryNode();
    }

    @Override
    public void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<ArgumentPosition> optionalPosition,
                         boolean mustBeNew) throws IllegalTreeUpdateException {
        TreeNode parentNode = nodeIndex.get(parentQueryNode);
        if (parentNode == null) {
            throw new IllegalTreeUpdateException("Node " + parentQueryNode + " not in the graph");
        }

        TreeNode childNode;
        if (nodeIndex.containsKey(childQueryNode)) {
            if (mustBeNew) {
                throw new IllegalTreeUpdateException("Node " + childQueryNode + " already in the graph");
            }
            else {
                childNode = nodeIndex.get(childQueryNode);

                TreeNode previousParent = parentIndex.get(childNode);
                if (previousParent != null) {
                    removeChild(previousParent, childNode);
                }
                parentIndex.put(childNode, parentNode);
                childrenIndex.get(parentNode).addChild(childNode, optionalPosition);
            }
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
    private void createNewNode(QueryNode childQueryNode, TreeNode parentNode, Optional<ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException {
        TreeNode childNode = new TreeNode(childQueryNode);
        nodeIndex.put(childQueryNode, childNode);

        ChildrenRelation newChildrenRelation;
        if (childQueryNode instanceof BinaryChildrenRelation) {
            newChildrenRelation = new BinaryChildrenRelation(childNode);
        }
        else {
            newChildrenRelation = new StandardChildrenRelation(childNode);
        }
        childrenIndex.put(childNode, newChildrenRelation);

        parentIndex.put(childNode, parentNode);
        childrenIndex.get(parentNode).addChild(childNode, optionalPosition);
    }

    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        ChildrenRelation childrenRelation = childrenIndex.get(node);
        if (childrenRelation == null) {
            return ImmutableList.of();
        }
        else {
            return childrenRelation.getChildQueryNodes();
        }
    }

    @Override
    public boolean contains(QueryNode node) {
        return nodeIndex.containsKey(node);
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() {
        return getNodesInTopDownOrder().reverse();
    }

    @Override
    public ImmutableList<QueryNode> getNodesInTopDownOrder() {
        Queue<TreeNode> nodesToExplore = new LinkedList<>();
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        nodesToExplore.add(rootNode);
        builder.add(rootNode.getQueryNode());

        while (!nodesToExplore.isEmpty()) {
            TreeNode node = nodesToExplore.poll();
            for (TreeNode childNode : childrenIndex.get(node).getChildren()) {
                nodesToExplore.add(childNode);
                builder.add(childNode.getQueryNode());
            }
        }
        return builder.build();
    }

    @Override
    public void replaceNode(QueryNode previousNode, QueryNode replacingNode) {
        TreeNode treeNode = nodeIndex.get(previousNode);
        if (treeNode == null) {
            throw new IllegalArgumentException("The previous query node must be in the tree");
        }
        if (contains(replacingNode)) {
            throw new IllegalArgumentException("The replacing node must not be already in the tree");
        }

        treeNode.changeQueryNode(replacingNode);
        nodeIndex.remove(previousNode);
        nodeIndex.put(replacingNode, treeNode);
    }

    @Override
    public void removeSubTree(QueryNode subQueryTreeRoot) {
        TreeNode subTreeRoot = nodeIndex.get(subQueryTreeRoot);
        if (subTreeRoot == null)
            throw new IllegalArgumentException("The given query node must be in the tree");

        Queue<TreeNode> nodesToRemove = new LinkedList<>();
        nodesToRemove.add(subTreeRoot);

        while(!nodesToRemove.isEmpty()) {
            TreeNode treeNode = nodesToRemove.poll();
            nodesToRemove.addAll(childrenIndex.get(treeNode).getChildren());

            removeNode(treeNode);
        }
    }

    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentQueryNode) {
        TreeNode currentTreeNode = nodeIndex.get(currentQueryNode);

        if (currentQueryNode == null)
            throw new IllegalArgumentException("The given query node must be in the tree");

        Queue<TreeNode> nodesToExplore = new LinkedList<>();
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        nodesToExplore.add(currentTreeNode);
        // The root is excluded from the list

        while (!nodesToExplore.isEmpty()) {
            TreeNode node = nodesToExplore.poll();
            for (TreeNode childNode : childrenIndex.get(node).getChildren()) {
                nodesToExplore.add(childNode);
                builder.add(childNode.getQueryNode());
            }
        }
        return builder.build();
    }

    @Override
    public Optional<QueryNode> getParent(QueryNode childQueryNode) {
        TreeNode childTreeNode = nodeIndex.get(childQueryNode);
        if (childTreeNode == null)
            throw new IllegalArgumentException("The given query node must be in the tree");
        TreeNode parentTreeNode = parentIndex.get(childTreeNode);
        if (parentTreeNode == null) {
            return Optional.absent();
        }
        else {
            return Optional.of(parentTreeNode.getQueryNode());
        }
    }

    @Override
    public void removeOrReplaceNodeByUniqueChild(QueryNode parentQueryNode) throws IllegalTreeUpdateException {
        TreeNode parentTreeNode = getTreeNode(parentQueryNode);

        ImmutableList<TreeNode> children = childrenIndex.get(parentTreeNode).getChildren();

        if (children.size() == 1) {
            TreeNode childTreeNode = children.get(0);
            childrenIndex.remove(parentTreeNode);
            // May be null
            TreeNode grandParentTreeNode = parentIndex.get(parentTreeNode);
            parentIndex.remove(parentTreeNode);
            parentIndex.put(childTreeNode, grandParentTreeNode);

            ChildrenRelation grandParentRelation = childrenIndex.get(grandParentTreeNode);
            grandParentRelation.replaceChild(parentTreeNode, childTreeNode);
        }
        else {
            throw new IllegalTreeUpdateException("The query node " + parentQueryNode + " does not have a unique child");
        }
    }

    @Override
    public void replaceNodesByOneNode(ImmutableList<QueryNode> nodesToRemove, QueryNode replacingNode,
                                      QueryNode parentNode, Optional<ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException {
        if (replacingNode instanceof BinaryAsymmetricOperatorNode) {
            throw new RuntimeException("Having a BinaryAsymmetricOperatorNode replacing node is not yet supported");
        }
        addChild(parentNode, replacingNode, optionalPosition, true);


        for(QueryNode nodeToRemove : nodesToRemove) {
            boolean isParentBinaryAsymmetricOperator = (nodeToRemove instanceof BinaryAsymmetricOperatorNode);

            TreeNode treeNodeToRemove = getTreeNode(nodeToRemove);

            for (QueryNode child : childrenIndex.get(treeNodeToRemove).getChildQueryNodes()) {
                if (!nodesToRemove.contains(child)) {
                    if (isParentBinaryAsymmetricOperator) {
                        throw new RuntimeException("Re-integrating children of a BinaryAsymmetricOperatorNode " +
                                "is not yet supported");
                    }
                    else {
                        addChild(replacingNode, child, Optional.<ArgumentPosition>absent(), false);
                    }
                }
            }
            nodeIndex.remove(nodeToRemove);

            TreeNode localParentTreeNode = parentIndex.get(treeNodeToRemove);
            if (localParentTreeNode != null) {
                ChildrenRelation childrenRelation = childrenIndex.get(localParentTreeNode);
                if (childrenRelation.contains(treeNodeToRemove)) {
                    childrenRelation.removeChild(treeNodeToRemove);
                }
            }
            parentIndex.remove(treeNodeToRemove);
            childrenIndex.remove(treeNodeToRemove);
        }
    }

    @Override
    public Optional<ArgumentPosition> getOptionalPosition(QueryNode parentNode, QueryNode childNode) {
        TreeNode parentTreeNode = getTreeNode(parentNode);
        TreeNode childTreeNode = getTreeNode(childNode);

        ChildrenRelation childrenRelation = childrenIndex.get(parentTreeNode);
        return childrenRelation.getOptionalPosition(childTreeNode);
    }

    /**
     * Low-level
     */
    private void removeNode(TreeNode treeNode) {
        nodeIndex.remove(treeNode.getQueryNode());
        TreeNode parentNode = parentIndex.get(treeNode);
        if (parentNode != null) {
            childrenIndex.get(parentNode).removeChild(treeNode);
        }
        parentIndex.remove(treeNode);
        childrenIndex.remove(treeNode);

    }

    private void removeChild(TreeNode parentNode, TreeNode childNodeToRemove) {
        if (parentIndex.get(childNodeToRemove) == parentNode) {
            parentIndex.remove(childNodeToRemove);
        }

        if (childrenIndex.containsKey(parentNode)) {
            childrenIndex.get(parentNode).removeChild(childNodeToRemove);
        }
    }

    private TreeNode getTreeNode(QueryNode node) {
        TreeNode treeNode = nodeIndex.get(node);
        if (treeNode == null) {
            throw new IllegalArgumentException("The given query node is not in the tree");
        }
        return treeNode;
    }
}
