package it.unibz.inf.ontop.iq.impl.tree;

import it.unibz.inf.ontop.iq.node.QueryNode;

import java.util.Map;

/**
 * Mutable and low-level.
 */
public class TreeNode {

    private QueryNode queryNode;

    protected TreeNode(QueryNode queryNode) {
        this.queryNode = queryNode;
    }

    protected QueryNode getQueryNode() {
        return queryNode;
    }

    @Override
    public String toString() {
        return "TN(" + queryNode + ")";
    }

    /**
     * Does not clone the query node
     */
    public TreeNode cloneShallowly() {
        return new TreeNode(queryNode);
    }

    public TreeNode findNewTreeNode(Map<QueryNode, TreeNode> newNodeIndex) {
        return newNodeIndex.get(queryNode);
    }
}
