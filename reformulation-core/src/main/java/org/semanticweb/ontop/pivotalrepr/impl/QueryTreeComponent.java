package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.pivotalrepr.*;

import java.util.Optional;

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

    Optional<NonCommutativeOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
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
                               Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException;

    /**
     * Please consider using an IntermediateQueryBuilder instead of this tree component.
     */
    void addChild(QueryNode parentNode, QueryNode childNode,
                  Optional<NonCommutativeOperatorNode.ArgumentPosition> optionalPosition,
                  boolean canReplacePreviousChildren) throws IllegalTreeUpdateException;

    Optional<QueryNode> nextSibling(QueryNode node) throws IllegalTreeException;

    Optional<QueryNode> getFirstChild(QueryNode node);

    /**
     * Inserts a new node between a node and its former parent (now grand-parent)
     */
    void insertParent(QueryNode childNode, QueryNode newParentNode) throws IllegalTreeUpdateException;

    /**
     * Returns a variable that is not used in the intermediate query.
     */
    Variable generateNewVariable();

    /**
     * Returns a variable that is not used in the intermediate query.
     *
     * The new variable always differs from the former one.
     *
     */
    Variable generateNewVariable(Variable formerVariable);

    /**
     * All the possibly already allocated variables
     */
    ImmutableSet<Variable> getKnownVariables();
}
