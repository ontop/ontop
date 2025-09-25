package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

/**
 * Temporary QueryNode that says that replace a non-satisfied sub-tree.
 *
 * Is expected to remove quickly.
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface EmptyNode extends LeafIQTree {

    @Override
    EmptyNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.transformEmpty(this);
    }
}
