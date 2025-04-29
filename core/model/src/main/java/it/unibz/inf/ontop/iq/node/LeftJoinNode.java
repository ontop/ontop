package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface LeftJoinNode extends JoinLikeNode, BinaryNonCommutativeOperatorNode {

    @Override
    LeftJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition);

    @Override
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree leftChild, IQTree rightChild) {
        return transformer.transformLeftJoin(tree,this, leftChild, rightChild);
    }

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor, IQTree leftChild, IQTree rightChild) {
        return visitor.visitLeftJoin(this, leftChild, rightChild);
    }

    @Override
    default LeftJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }
}
