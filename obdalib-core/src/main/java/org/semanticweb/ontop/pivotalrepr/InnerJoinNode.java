package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import org.semanticweb.ontop.model.ImmutableBooleanExpression;

public interface InnerJoinNode extends CommutativeJoinNode {

    @Override
    InnerJoinNode clone();

    @Override
    InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableBooleanExpression> newOptionalFilterCondition);
}
