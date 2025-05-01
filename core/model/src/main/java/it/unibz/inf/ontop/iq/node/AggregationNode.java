package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.Variable;
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
    default IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformAggregation(tree, this, child);
    }

    @Override
    default <T> T acceptVisitor(IQTree tree, IQVisitor<T> visitor, IQTree child) {
        return visitor.transformAggregation(tree, this, child);
    }

}
