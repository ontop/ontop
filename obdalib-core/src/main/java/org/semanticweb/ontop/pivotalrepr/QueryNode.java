package org.semanticweb.ontop.pivotalrepr;

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

    public BooleanOperator getFilterExpression();
    public boolean hasFilterExpression();

    public boolean isRejected();
}
