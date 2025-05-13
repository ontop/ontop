package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

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


    @Override
    default <T> T acceptVisitor(UnaryIQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.transformFilter(tree, this, child);
    }
}
