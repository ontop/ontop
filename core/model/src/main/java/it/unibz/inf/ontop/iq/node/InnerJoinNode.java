package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface InnerJoinNode extends InnerJoinLikeNode {

    @Override
    InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition);

    @Override
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, ImmutableList<IQTree> children) {
        return transformer.transformInnerJoin(tree,this, children);
    }

    @Override
    default <T> T acceptVisitor(IQTree tree, IQVisitor<T> visitor, ImmutableList<IQTree> children) {
        return visitor.transformInnerJoin(tree, this, children);
    }

    @Override
    default InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }
}
