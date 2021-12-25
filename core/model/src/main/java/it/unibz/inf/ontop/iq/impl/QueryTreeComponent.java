package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.IllegalTreeException;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Mutable component used for internal implementations of IntermediateQuery.
 */
public interface QueryTreeComponent {

    ImmutableList<QueryNode> getChildren(QueryNode node);

    Stream<QueryNode> getChildrenStream(QueryNode node);

    QueryNode getRootNode() throws IllegalTreeException;

    ImmutableList<QueryNode> getNodesInTopDownOrder() throws IllegalTreeException;

    ImmutableSet<TrueNode> getTrueNodes();

    ImmutableSet<IntensionalDataNode> getIntensionalNodes();

    boolean contains(QueryNode node);

    void replaceNode(QueryNode previousNode, QueryNode replacingNode);

    void removeSubTree(QueryNode subTreeRoot);

    Optional<BinaryOrderedOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
                                                                             QueryNode childNode);

    Optional<QueryNode> getParent(QueryNode node) throws IllegalTreeException;

    /**
     * Please consider using an IntermediateQueryBuilder instead of this tree component.
     */
    void addChild(QueryNode parentNode, QueryNode childNode,
                  Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition,
                  boolean canReplacePreviousChildren) throws IllegalTreeUpdateException;

    Optional<QueryNode> nextSibling(QueryNode node) throws IllegalTreeException;

    /**
     * Inserts a new node between a node and its former parent (now grand-parent)
     */
    void insertParent(QueryNode childNode, QueryNode newParentNode) throws IllegalTreeUpdateException;

    /**
     * Inserts a new node between a node and its former parent (now grand-parent)
     */
    void insertParent(QueryNode childNode, QueryNode newParentNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition)
            throws IllegalTreeUpdateException;

    /**
     * All the possibly already allocated variables
     */
    ImmutableSet<Variable> getKnownVariables();

    /**
     * If no position is given, replaces the parent node by its first child
     */
    QueryNode replaceNodeByChild(QueryNode parentNode,
                                 Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalReplacingChildPosition);


    /**
     * Keeps the same query node objects but clones the tree edges
     * (since the latter are mutable by default).
     */
    QueryTreeComponent createSnapshot();

    ImmutableSet<Variable> getVariables(QueryNode node);
}
