package org.semanticweb.ontop.pivotalrepr;

import org.semanticweb.ontop.model.ImmutableBooleanExpression;

/**
 * TODO: explain
 */
public interface FilterNode extends JoinOrFilterNode {

    @Override
    FilterNode clone();

    @Override
    FilterNode acceptNodeTransformer(QueryNodeTransformer transformer) throws QueryNodeTransformationException;

    /**
     * Not optional for a FilterNode.
     */
    ImmutableBooleanExpression getFilterCondition();
}
