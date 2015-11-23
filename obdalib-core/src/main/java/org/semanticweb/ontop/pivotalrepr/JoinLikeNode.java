package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;

/**
 * Any kind of JOIN (inner, left join)
 */
public interface JoinLikeNode extends JoinOrFilterNode {
    /**
     * Constructs a new JoinLikeNode with the following optional filter condition
     * (recall that a QueryNode is immutable).
     */
    JoinLikeNode changeOptionalFilterCondition(Optional<ImmutableBooleanExpression> newOptionalFilterCondition);
}
