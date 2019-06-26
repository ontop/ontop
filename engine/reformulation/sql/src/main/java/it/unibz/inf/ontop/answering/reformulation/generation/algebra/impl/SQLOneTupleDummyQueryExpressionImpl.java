package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLOneTupleDummyQueryExpression;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;

public class SQLOneTupleDummyQueryExpressionImpl implements SQLOneTupleDummyQueryExpression {

    @Inject
    private SQLOneTupleDummyQueryExpressionImpl() {
    }


    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
