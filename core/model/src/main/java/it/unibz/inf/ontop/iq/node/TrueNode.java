package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

/**
 * See {@link IntermediateQueryFactory#createTrueNode()} for creating a new instance.
 */
public interface TrueNode extends LeafIQTree {

    @Override
    TrueNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.transformTrue(this);
    }
}
