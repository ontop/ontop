package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.*;

import java.util.Optional;

/**
 * TODO: describe
 */
public interface QueryTree {
    QueryNode getRootNode();

    void addChild(QueryNode parentQueryNode, QueryNode childQueryNode) throws IllegalTreeUpdateException;

    ImmutableList<QueryNode> getChildren(QueryNode node);
}
