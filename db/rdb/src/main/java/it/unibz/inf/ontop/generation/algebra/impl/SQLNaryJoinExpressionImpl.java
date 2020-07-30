package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.generation.algebra.SQLNaryJoinExpression;
import it.unibz.inf.ontop.generation.algebra.SQLExpression;
import it.unibz.inf.ontop.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

public class SQLNaryJoinExpressionImpl implements SQLNaryJoinExpression {
    private final ImmutableList<SQLExpression> joinedExpressions;

    @AssistedInject
    private SQLNaryJoinExpressionImpl(@Assisted ImmutableList<SQLExpression> joinedExpressions) {
        this.joinedExpressions = joinedExpressions;
    }

    @Override
    public ImmutableList<SQLExpression> getJoinedExpressions() {
        return this.joinedExpressions;
    }

    @Override
    public Optional<ImmutableExpression> getFilterCondition() {
        return Optional.empty();
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
