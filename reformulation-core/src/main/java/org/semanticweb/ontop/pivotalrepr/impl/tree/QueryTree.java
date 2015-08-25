package org.semanticweb.ontop.pivotalrepr.impl.tree;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.BinaryAsymmetricOperatorNode;
import org.semanticweb.ontop.pivotalrepr.ConstructionNode;
import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;
import org.semanticweb.ontop.pivotalrepr.QueryNode;
import org.semanticweb.ontop.pivotalrepr.impl.IllegalTreeUpdateException;

/**
 * TODO: describe
 */
public interface QueryTree {
    ConstructionNode getRootNode();

    void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition, boolean mustBeNew) throws IllegalTreeUpdateException;

    ImmutableList<QueryNode> getChildren(QueryNode node);

    boolean contains(QueryNode node);

    ImmutableList<QueryNode> getNodesInBottomUpOrder();

    ImmutableList<QueryNode> getNodesInTopDownOrder();

    void replaceNode(QueryNode previousNode, QueryNode replacingNode, boolean isNewNode);

    void removeSubTree(QueryNode subTreeRoot);

    ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode);

    Optional<QueryNode> getParent(QueryNode childNode);

}
