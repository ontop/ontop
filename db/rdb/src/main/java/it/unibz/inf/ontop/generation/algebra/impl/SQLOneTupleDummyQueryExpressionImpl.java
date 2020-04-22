package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.algebra.SQLOneTupleDummyQueryExpression;
import it.unibz.inf.ontop.generation.algebra.SQLRelationVisitor;

public class SQLOneTupleDummyQueryExpressionImpl implements SQLOneTupleDummyQueryExpression {

    @Inject
    private SQLOneTupleDummyQueryExpressionImpl() {
    }


    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
