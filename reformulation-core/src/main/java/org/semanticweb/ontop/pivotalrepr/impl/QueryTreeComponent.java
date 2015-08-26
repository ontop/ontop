package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.List;

/**
 * Mutable component used for internal implementations of IntermediateQuery.
 */
public interface QueryTreeComponent {

    ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

    ConstructionNode getRootConstructionNode() throws IllegalTreeException;

    ImmutableList<QueryNode> getNodesInBottomUpOrder() throws IllegalTreeException;

    ImmutableList<QueryNode> getNodesInTopDownOrder() throws IllegalTreeException;

    boolean contains(QueryNode node);

    void replaceNode(QueryNode previousNode, QueryNode replacingNode);

    void addSubTree(IntermediateQuery subQuery, QueryNode subQueryTopNode, QueryNode localTopNode)
            throws IllegalTreeUpdateException;

    void removeSubTree(QueryNode subTreeRoot);

    /**
     * All the nodes EXCEPT the root of this sub-tree
     */
    ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode);

    Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
                                                                               QueryNode childNode);

    /**
     * From the parent to the oldest ancestor.
     */
    ImmutableList<QueryNode> getAncestors(QueryNode descendantNode) throws IllegalTreeException;

    Optional<QueryNode> getParent(QueryNode node) throws IllegalTreeException;

    /**
     * TODO: explain
     */
    void removeOrReplaceNodeByUniqueChildren(QueryNode node) throws IllegalTreeUpdateException;

    /**
     * TODO: explain
     */
    void replaceNodesByOneNode(ImmutableList<QueryNode> queryNodes, QueryNode replacingNode, QueryNode parentNode,
                               Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException;

    /**
     * Please consider using an IntermediateQueryBuilder instead of this tree component.
     */
    void addChild(QueryNode parentNode, QueryNode childNode,
                  Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> optionalPosition) throws IllegalTreeUpdateException;

    Optional<QueryNode> nextSibling(QueryNode node) throws IllegalTreeException;

    Optional<QueryNode> getFirstChild(QueryNode node);
}
