package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLSerializedQuery;

public class SQLSerializedQueryImpl implements SQLSerializedQuery {

    private final String sqlQueryString;

    @AssistedInject
    private SQLSerializedQueryImpl(@Assisted String sqlString) {
        this.sqlQueryString = sqlString;
    }

    @Override
    public String getSQLString() {
        return sqlQueryString;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
