package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;

import java.util.Optional;

/**
 * TODO: find a better name
 *
 * See SQLAlgebraFactory for creating a new instance.
 *
 */
public interface SelectFromWhereWithModifiers extends SQLExpression {

    /**
     * The order in the ImmutableSet is the insertion order and may matter
     */
    ImmutableSet<Variable> getProjectedVariables();

    Substitution<? extends ImmutableTerm> getSubstitution();

    SQLExpression getFromSQLExpression();

    Optional<ImmutableExpression> getWhereExpression();

    ImmutableSet<Variable> getGroupByVariables();

    boolean isDistinct();
    Optional<Long> getLimit();
    Optional<Long> getOffset();

    ImmutableList<SQLOrderComparator> getSortConditions();

    default boolean hasOrder() {
        return !getSortConditions().isEmpty();
    }

}
