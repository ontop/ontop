package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

public interface InnerJoinLikeNode extends CommutativeJoinNode {

    @Override
    InnerJoinLikeNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    InnerJoinLikeNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition);
}
