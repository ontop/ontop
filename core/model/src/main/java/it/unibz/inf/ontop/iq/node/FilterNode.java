package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;

/**
 * TODO: explain
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface FilterNode extends CommutativeJoinOrFilterNode, UnaryOperatorNode {

    /**
     * Not optional for a FilterNode.
     */
    ImmutableExpression getFilterCondition();

    /**
     * Returns a new FilterNode (immutable).
     */
    FilterNode changeFilterCondition(ImmutableExpression newFilterCondition);

    @Override
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformFilter(tree,this, child);
    }

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitFilter(this, child);
    }

}
