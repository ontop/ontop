package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

/**
 * TODO: find a better name
 *
 * See SQLAlgebraFactory for creating a new instance.
 *
 */
public interface SelectFromWhereWithModifiers extends SQLExpression {

    ImmutableSortedSet<Variable> getProjectedVariables();

    ImmutableSubstitution<? extends ImmutableTerm> getSubstitution();

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
