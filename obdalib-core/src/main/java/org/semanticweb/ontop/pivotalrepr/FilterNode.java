package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.BooleanExpression;

/**
 * TODO: explain
 */
public interface FilterNode extends QueryNode {

    public BooleanExpression getFilterCondition();

    public boolean hasFilterCondition();
}
