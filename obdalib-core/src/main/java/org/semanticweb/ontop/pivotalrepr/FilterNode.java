package org.semanticweb.ontop.pivotalrepr;

/**
 * TODO: explain
 */
public interface FilterNode extends QueryNode {

    /**
     * TODO: should it be here?
     */
    public BooleanExpression getFilterExpression();

    /**
     * TODO: should it be here?
     */
    public boolean hasFilterExpression();
}
