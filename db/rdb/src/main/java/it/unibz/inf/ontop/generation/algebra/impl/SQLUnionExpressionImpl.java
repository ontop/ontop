package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.generation.algebra.SQLExpression;
import it.unibz.inf.ontop.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.generation.algebra.SQLUnionExpression;
import it.unibz.inf.ontop.model.term.Variable;

public class SQLUnionExpressionImpl implements SQLUnionExpression {

    private final ImmutableList<SQLExpression> subExpressions;
    private final ImmutableSet<Variable> projectedVariables;

    @AssistedInject
    private SQLUnionExpressionImpl(@Assisted ImmutableList<SQLExpression> subExpressions, @Assisted ImmutableSet<Variable> projectedVariables) {
        this.subExpressions = subExpressions;
        this.projectedVariables = projectedVariables;
    }

    @Override
    public ImmutableSet<Variable> getProjectedVariables() {
        return this.projectedVariables;
    }

    @Override
    public ImmutableList<? extends SQLExpression> getSubExpressions() {
        return this.subExpressions;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

