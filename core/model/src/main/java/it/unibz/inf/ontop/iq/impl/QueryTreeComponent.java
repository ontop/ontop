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

    /**
     * Please consider using an IntermediateQueryBuilder instead of this tree component.
     */
    void addChild(QueryNode parentNode, QueryNode childNode,
                  Optional<BinaryOrderedOperatorNode.ArgumentPosition> optionalPosition,
                  boolean canReplacePreviousChildren) throws IllegalTreeUpdateException;

    /**
     * Keeps the same query node objects but clones the tree edges
     * (since the latter are mutable by default).
     */
    QueryTreeComponent createSnapshot();

    ImmutableSet<Variable> getVariables(QueryNode node);
}
