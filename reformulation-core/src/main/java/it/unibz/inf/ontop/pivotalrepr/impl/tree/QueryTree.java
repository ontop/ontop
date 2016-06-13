package it.unibz.inf.ontop.pivotalrepr.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.ConstructionNode;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;

import java.util.Optional;

/**
 * TODO: describe
 */
public interface QueryTree {
    ConstructionNode getRootNode();

    void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<ArgumentPosition> optionalPosition,
                  boolean mustBeNew, boolean canReplace) throws IllegalTreeUpdateException;

    ImmutableList<QueryNode> getChildren(QueryNode node);

    boolean contains(QueryNode node);

    ImmutableList<QueryNode> getNodesInBottomUpOrder();

    ImmutableList<QueryNode> getNodesInTopDownOrder();

    void replaceNode(QueryNode previousNode, QueryNode replacingNode);

    void removeSubTree(QueryNode subTreeRoot);

    /**
     * Excludes the top node from the list
     */
    ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode);

    Optional<QueryNode> getParent(QueryNode childNode);

    void removeOrReplaceNodeByUniqueChild(QueryNode node) throws IllegalTreeUpdateException;

    void replaceNodesByOneNode(ImmutableList<QueryNode> queryNodes, QueryNode replacingNode, QueryNode parentNode, Optional<ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException;

    Optional<ArgumentPosition> getOptionalPosition(QueryNode parentNode, QueryNode childNode);

    void insertParent(QueryNode childNode, QueryNode newParentNode) throws IllegalTreeUpdateException;
}
