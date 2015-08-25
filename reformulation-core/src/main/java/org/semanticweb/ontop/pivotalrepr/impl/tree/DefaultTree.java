package org.semanticweb.ontop.pivotalrepr.impl.tree;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode.ArgumentPosition;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;

import java.util.HashMap;
import java.util.Map;

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
            }
        }
        /**
         * New node
         */
        else {
            childNode = new TreeNode(childQueryNode);
            nodeIndex.put(childQueryNode, childNode);

            ChildrenRelation newChildrenRelation;
            if (childQueryNode instanceof BinaryChildrenRelation) {
                newChildrenRelation = new BinaryChildrenRelation(childNode);
            }
            else {
                newChildrenRelation = new StandardChildrenRelation(childNode);
            }
            childrenIndex.put(childNode, newChildrenRelation);
        }

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

    private void removeChild(TreeNode parentNode, TreeNode childNodeToRemove) {
        if (parentIndex.get(childNodeToRemove) == parentNode) {
            parentIndex.remove(childNodeToRemove);
        }

        if (childrenIndex.containsKey(parentNode)) {
            childrenIndex.get(parentNode).removeChild(childNodeToRemove);
        }
    }
}
