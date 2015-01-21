package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;

/**
 * Mutable (under certain conditions).
 *
 * Why mutable? Because of the tree structure.
 *
 *
 * TODO: complete it
 */
public interface QueryNode {

    public QueryModifiers getModifiers();
    public boolean hasModifiers();

    /**
     * Apply optimization locally (no "navigation with side-effects"??)
     */
    public void applyOptimization(QueryOptimizer optimizer);

    public BooleanExpression getFilterExpression();
    public boolean hasFilterExpression();

    public boolean isRejected();

    /**
     * TODO: throw an exception if the node is not in the DAG anymore.
     *
     * Follows the evolution of the DAG ("dynamic").
     *
     */
    public ImmutableList<QueryNode> getCurrentSubNodes();
}
