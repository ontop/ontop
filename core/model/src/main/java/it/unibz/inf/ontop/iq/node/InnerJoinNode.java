package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

/**
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface InnerJoinNode extends InnerJoinLikeNode {

    @Override
    InnerJoinNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    @Override
    default <T> T acceptVisitor(NaryIQTree tree, IQTreeVisitor<T> visitor, ImmutableList<IQTree> children) {
        return visitor.transformInnerJoin(tree, this, children);
    }
}
