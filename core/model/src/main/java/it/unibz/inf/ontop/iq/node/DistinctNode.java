package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

/**
 * See {@link IntermediateQueryFactory#createDistinctNode()} for creating a new instance.
 */
public interface DistinctNode extends QueryModifierNode {

    @Override
    DistinctNode applyFreshRenaming(InjectiveSubstitution<Variable> freshRenamingSubstitution);

    @Override
    default <T> T acceptVisitor(UnaryIQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.transformDistinct(tree, this, child);
    }
}
