package it.unibz.inf.ontop.pivotalrepr.impl.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.IllegalTreeUpdateException;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO: describe
 */
public interface QueryTree {
    ConstructionNode getRootNode();

    void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition,
                  boolean mustBeNew, boolean canReplace) throws IllegalTreeUpdateException;

    ImmutableList<QueryNode> getChildren(QueryNode node);

    Stream<QueryNode> getChildrenStream(QueryNode node);

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

    QueryNode removeOrReplaceNodeByUniqueChild(QueryNode node) throws IllegalTreeUpdateException;

    void replaceNodesByOneNode(ImmutableList<QueryNode> queryNodes, QueryNode replacingNode, QueryNode parentNode,
                               Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException;

    Optional<NonCommutativeOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode, QueryNode childNode);

    void insertParent(QueryNode childNode, QueryNode newParentNode) throws IllegalTreeUpdateException;

    ImmutableSet<EmptyNode> getEmptyNodes();

    QueryNode replaceNodeByChild(QueryNode parentNode,
                                 Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalReplacingChildPosition);

    ImmutableSet<IntensionalDataNode> getIntensionalNodes();


    /**
     * Keeps the same query node objects but clones the tree edges
     * (since the latter are mutable by default).
     */
    QueryTree createSnapshot();
}
