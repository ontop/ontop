package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.visit.IQTreeVisitor;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;

/**
 * TODO: explain
 * TODO: find a better name
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface IntensionalDataNode extends LeafIQTree {

    DataAtom<AtomPredicate> getProjectionAtom();

    @Override
    IntensionalDataNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    @Override
    default <T> T acceptVisitor(IQTreeVisitor<T> visitor) {
        return visitor.transformIntensionalData(this);
    }
}
