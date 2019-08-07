package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLExpression;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLInnerJoinExpression;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

public class SQLInnerJoinExpressionImpl implements SQLInnerJoinExpression {

    private final SQLExpression leftExpression;
    private final SQLExpression rightExpression;

    @AssistedInject
    private SQLInnerJoinExpressionImpl(@Assisted("leftExpression") SQLExpression leftExpression, @Assisted("rightExpression") SQLExpression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    @Override
    public SQLExpression getLeft() {
        return this.leftExpression;
    }

    @Override
    public SQLExpression getRight() {
        return this.rightExpression;
    }

    @Override
    public Optional<ImmutableExpression> getFilterCondition() {
        return Optional.empty();
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ImmutableList<? extends SQLExpression> getSubExpressions() {
        return ImmutableList.of(this.leftExpression, this.rightExpression);
    }
}
