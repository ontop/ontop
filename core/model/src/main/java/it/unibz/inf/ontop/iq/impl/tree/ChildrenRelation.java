package it.unibz.inf.ontop.iq.impl.tree;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.exception.IllegalTreeUpdateException;
import it.unibz.inf.ontop.iq.node.QueryNode;

/**
 * TODO: explain
 */
public interface ChildrenRelation {

    ImmutableList<QueryNode> getChildren();

    void addChild(QueryNode childNode) throws IllegalTreeUpdateException;
}
