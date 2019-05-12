package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLExpression;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.iq.node.OrderByNode;
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
    private final ImmutableList<? extends SQLExpression> fromRelations;
    private final Optional<ImmutableExpression> whereExpression;
    private final boolean isDistinct;
    private final Optional<Long> limit;
    private final Optional<Long> offset;
    private final ImmutableList<OrderByNode.OrderComparator> sortConditions;

    @AssistedInject
    private SelectFromWhereWithModifiersImpl(@Assisted ImmutableSortedSet<Variable> projectedVariables,
                                             @Assisted ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                             @Assisted("fromRelations") ImmutableList<? extends SQLExpression> fromRelations,
                                             @Assisted("whereExpression") Optional<ImmutableExpression> whereExpression,
                                             @Assisted boolean isDistinct,
                                             @Assisted("limit") Optional<Long> limit,
                                             @Assisted("offset") Optional<Long> offset,
                                             @Assisted("sortConditions") ImmutableList<OrderByNode.OrderComparator> sortConditions) {
        this.projectedVariables = projectedVariables;
        this.substitution = substitution;
        this.fromRelations = fromRelations;
        this.whereExpression = whereExpression;
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
    public ImmutableList<? extends SQLExpression> getFromSQLExpressions() {
        return fromRelations;
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
    public ImmutableList<OrderByNode.OrderComparator> getSortConditions() {
        return sortConditions;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
