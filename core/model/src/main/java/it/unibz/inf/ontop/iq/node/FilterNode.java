package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

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
    FilterNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    @Override
    default <T> T acceptVisitor(UnaryIQTree tree, IQTreeVisitor<T> visitor, IQTree child) {
        return visitor.transformFilter(tree, this, child);
    }
}
