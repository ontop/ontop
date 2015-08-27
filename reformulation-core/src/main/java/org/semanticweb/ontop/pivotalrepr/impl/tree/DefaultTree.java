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
        nodeIndex = new HashMap<>();
        childrenIndex = new HashMap<>();
        parentIndex = new HashMap<>();

        // Adds the root node
        rootNode = new TreeNode(rootQueryNode);
        nodeIndex.put(rootQueryNode, rootNode);
        childrenIndex.put(rootNode, createChildrenRelation(rootNode, rootQueryNode));
        // No parent
    }

    @Override
    public ConstructionNode getRootNode() {
        return (ConstructionNode) rootNode.getQueryNode();
    }

    @Override
    public void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<ArgumentPosition> optionalPosition,
                         boolean mustBeNew) throws IllegalTreeUpdateException {
        TreeNode parentNode = getTreeNode(parentQueryNode);

        TreeNode childNode;
        if (nodeIndex.containsKey(childQueryNode)) {
            if (mustBeNew) {
                throw new IllegalTreeUpdateException("Node " + childQueryNode + " already in the graph");
            }
            else {
                childNode = getTreeNode(childQueryNode);

                TreeNode previousParent = getParentTreeNode(childNode);
                if (previousParent != null) {
                    removeChild(previousParent, childNode);
                }
                parentIndex.put(childNode, parentNode);
                getChildrenRelation(parentNode).addChild(childNode, optionalPosition);
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

        childrenIndex.put(childNode, createChildrenRelation(childNode, childQueryNode));

        parentIndex.put(childNode, parentNode);
        getChildrenRelation(parentNode).addChild(childNode, optionalPosition);
    }

    private static ChildrenRelation createChildrenRelation(TreeNode parentTreeNode, QueryNode parentQueryNode) {
        if (parentQueryNode instanceof BinaryChildrenRelation) {
            return new BinaryChildrenRelation(parentTreeNode);
        }
        else {
            return new StandardChildrenRelation(parentTreeNode);
        }
    }


    @Override
    public ImmutableList<QueryNode> getChildren(QueryNode node) {
        ChildrenRelation childrenRelation = getChildrenRelation(getTreeNode(node));
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
            for (TreeNode childNode : getChildrenRelation(node).getChildren()) {
                nodesToExplore.add(childNode);
                builder.add(childNode.getQueryNode());
            }
        }
        return builder.build();
    }

    @Override
    public void replaceNode(QueryNode previousNode, QueryNode replacingNode) {
        TreeNode treeNode = getTreeNode(previousNode);
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
        TreeNode subTreeRoot = getTreeNode(subQueryTreeRoot);

        Queue<TreeNode> nodesToRemove = new LinkedList<>();
        nodesToRemove.add(subTreeRoot);

        while(!nodesToRemove.isEmpty()) {
            TreeNode treeNode = nodesToRemove.poll();
            nodesToRemove.addAll(getChildrenRelation(treeNode).getChildren());

            removeNode(treeNode);
        }
    }

    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentQueryNode) {
        TreeNode currentTreeNode = getTreeNode(currentQueryNode);

        Queue<TreeNode> nodesToExplore = new LinkedList<>();
        ImmutableList.Builder<QueryNode> builder = ImmutableList.builder();
        nodesToExplore.add(currentTreeNode);
        // The root is excluded from the list

        while (!nodesToExplore.isEmpty()) {
            TreeNode node = nodesToExplore.poll();
            for (TreeNode childNode : getChildrenRelation(node).getChildren()) {
                nodesToExplore.add(childNode);
                builder.add(childNode.getQueryNode());
            }
        }
        return builder.build();
    }

    @Override
    public Optional<QueryNode> getParent(QueryNode childQueryNode) {
        TreeNode childTreeNode = getTreeNode(childQueryNode);

        TreeNode parentTreeNode = getParentTreeNode(childTreeNode);
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

        ImmutableList<TreeNode> children = getChildrenRelation(parentTreeNode).getChildren();

        if (children.size() == 1) {
            TreeNode childTreeNode = children.get(0);
            childrenIndex.remove(parentTreeNode);
            // May be null
            TreeNode grandParentTreeNode = getParentTreeNode(parentTreeNode);
            parentIndex.remove(parentTreeNode);
            parentIndex.put(childTreeNode, grandParentTreeNode);

            ChildrenRelation grandParentRelation = getChildrenRelation(grandParentTreeNode);
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

            for (QueryNode child : getChildrenRelation(treeNodeToRemove).getChildQueryNodes()) {
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

            TreeNode localParentTreeNode = getParentTreeNode(treeNodeToRemove);
            if (localParentTreeNode != null) {
                ChildrenRelation childrenRelation = getChildrenRelation(localParentTreeNode);
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

        ChildrenRelation childrenRelation = getChildrenRelation(parentTreeNode);
        return childrenRelation.getOptionalPosition(childTreeNode);
    }

    /**
     * Low-level
     */
    private void removeNode(TreeNode treeNode) {
        nodeIndex.remove(treeNode.getQueryNode());
        TreeNode parentNode = getParentTreeNode(treeNode);
        if (parentNode != null) {
            getChildrenRelation(parentNode).removeChild(treeNode);
        }
        parentIndex.remove(treeNode);
        childrenIndex.remove(treeNode);

    }

    private void removeChild(TreeNode parentNode, TreeNode childNodeToRemove) {
        if (getParentTreeNode(childNodeToRemove) == parentNode) {
            parentIndex.remove(childNodeToRemove);
        }

        if (childrenIndex.containsKey(parentNode)) {
            getChildrenRelation(parentNode).removeChild(childNodeToRemove);
        }
    }

    private TreeNode getTreeNode(QueryNode node) {
        TreeNode treeNode = nodeIndex.get(node);
        if (treeNode == null) {
            throw new IllegalArgumentException("The given query node is not in the tree");
        }
        return treeNode;
    }

    private ChildrenRelation getChildrenRelation(TreeNode node) {
        ChildrenRelation relation = childrenIndex.get(node);
        if (relation == null) {
            throw new RuntimeException("Internal error: the tree node does not have a children relation.");
        }
        return relation;
    }

    /**
     * The returned value might be null.
     *
     * The point of this structure is to enforce the use of a TreeNode as argument.
     */
    private TreeNode getParentTreeNode(TreeNode child) {
        return parentIndex.get(child);
    }
}
