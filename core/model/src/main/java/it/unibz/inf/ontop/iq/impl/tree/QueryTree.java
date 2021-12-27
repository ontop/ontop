package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.*;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * TODO: describe
 */
public interface QueryTree {
    QueryNode getRootNode();

    void addChild(QueryNode parentQueryNode, QueryNode childQueryNode, Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition,
                  boolean canReplace) throws IllegalTreeUpdateException;

    ImmutableList<QueryNode> getChildren(QueryNode node);

    boolean contains(QueryNode node);

    ImmutableList<QueryNode> getNodesInTopDownOrder();

    /**
     * Keeps the same query node objects but clones the tree edges
     * (since the latter are mutable by default).
     */
    QueryTree createSnapshot();
}
