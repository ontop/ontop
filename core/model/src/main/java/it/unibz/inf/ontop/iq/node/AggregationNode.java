package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;


/**
 * Combines GROUP BY and a projection
 *
 * See IntermediateQueryFactory for creating a new instance.
 *
 */
public interface AggregationNode extends ExtendedProjectionNode {

    @Override
    Substitution<ImmutableFunctionalTerm> getSubstitution();

    ImmutableSet<Variable> getGroupingVariables();

    @Override
    AggregationNode applyFreshRenaming(InjectiveSubstitution<Variable> renamingSubstitution);

    @Override
    default <T> T acceptVisitor(UnaryIQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.transformAggregation(tree, this, child);
    }

}
