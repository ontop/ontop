package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;

/**
 * Commutative: the children order does not matter
 */
public interface CommutativeJoinNode extends JoinLikeNode {

    @Override
    CommutativeJoinNode changeOptionalFilterCondition(Optional<ImmutableBooleanExpression> newOptionalFilterCondition);
}
