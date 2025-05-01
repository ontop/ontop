package it.unibz.inf.ontop.iq.node;

import java.util.Optional;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

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
    default <T> T acceptVisitor(IQTree tree, IQVisitor<T> visitor, IQTree leftChild, IQTree rightChild) {
        return visitor.transformLeftJoin(tree, this, leftChild, rightChild);
    }

}
