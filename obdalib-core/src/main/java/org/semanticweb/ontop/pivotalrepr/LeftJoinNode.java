package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;

public interface LeftJoinNode extends JoinLikeNode, NonCommutativeOperatorNode {

    @Override
    LeftJoinNode clone();

    @Override
    LeftJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    LeftJoinNode changeOptionalFilterCondition(Optional<ImmutableBooleanExpression> newOptionalFilterCondition);
}
