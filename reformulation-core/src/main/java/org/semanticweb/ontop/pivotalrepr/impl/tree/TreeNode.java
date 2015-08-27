package org.semanticweb.ontop.pivotalrepr.impl.tree;

import org.semanticweb.ontop.pivotalrepr.QueryNode;

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

    protected void changeQueryNode(QueryNode newNode) {
        this.queryNode = newNode;
    }

    public String toString() {
        return "TN(" + queryNode + ")";
    }

}
