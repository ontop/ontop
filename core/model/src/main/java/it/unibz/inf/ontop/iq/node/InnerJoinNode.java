package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;


public interface InnerJoinNode extends InnerJoinLikeNode {

    @Override
    InnerJoinNode clone();

    @Override
    InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException;

    @Override
    InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition);
}
