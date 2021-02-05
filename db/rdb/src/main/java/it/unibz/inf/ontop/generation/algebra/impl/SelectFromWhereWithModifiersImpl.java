package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.generation.algebra.SQLExpression;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

/**
 * See SQLAlgebraFactory for creating a new instance.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SelectFromWhereWithModifiersImpl implements SelectFromWhereWithModifiers {

    private final ImmutableSortedSet<Variable> projectedVariables;
    private final ImmutableSubstitution<? extends ImmutableTerm> substitution;
    private final SQLExpression fromExpression;
    private final Optional<ImmutableExpression> whereExpression;
    private final ImmutableSet<Variable> groupByVariables;
    private final boolean isDistinct;
    private final Optional<Long> limit;
    private final Optional<Long> offset;
    private final ImmutableList<SQLOrderComparator> sortConditions;

    @AssistedInject
    private SelectFromWhereWithModifiersImpl(@Assisted ImmutableSortedSet<Variable> projectedVariables,
                                             @Assisted ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                             @Assisted("fromExpression") SQLExpression fromExpression,
                                             @Assisted("whereExpression") Optional<ImmutableExpression> whereExpression,
                                             @Assisted("groupBy") ImmutableSet<Variable> groupByVariables,
                                             @Assisted boolean isDistinct,
                                             @Assisted("limit") Optional<Long> limit,
                                             @Assisted("offset") Optional<Long> offset,
                                             @Assisted("sortConditions") ImmutableList<SQLOrderComparator> sortConditions) {
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.fromExpression = fromExpression;
        this.whereExpression = whereExpression;
        this.groupByVariables = groupByVariables;
        this.isDistinct = isDistinct;
        this.limit = limit;
        this.offset = offset;
        this.sortConditions = sortConditions;
    }


    @Override
    public ImmutableSortedSet<Variable> getProjectedVariables() {
        return projectedVariables;
    }

    @Override
    public ImmutableSubstitution<? extends ImmutableTerm> getSubstitution() {
        return substitution;
    }

    @Override
    public SQLExpression getFromSQLExpression() {
        return fromExpression;
    }

    @Override
    public Optional<ImmutableExpression> getWhereExpression() {
        return whereExpression;
    }

    @Override
    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    public Optional<Long> getLimit() {
        return limit;
    }

    @Override
    public Optional<Long> getOffset() {
        return offset;
    }

    @Override
    public ImmutableList<SQLOrderComparator> getSortConditions() {
        return sortConditions;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ImmutableSet<Variable> getGroupByVariables() {
        return groupByVariables;
    }
}
