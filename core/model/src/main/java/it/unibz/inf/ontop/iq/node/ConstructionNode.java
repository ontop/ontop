package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.substitution.Substitution;

/**
 * Head node an IntermediateQuery
 *
 * TODO: further explain
 *
 * See {@link IntermediateQueryFactory#createConstructionNode} for creating a new instance.
 *
 */
public interface ConstructionNode extends ExtendedProjectionNode {

    @Override
    Substitution<ImmutableTerm> getSubstitution();

    @Override
    default <T> T acceptVisitor(UnaryIQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.transformConstruction(tree, this, child);
    }

}
