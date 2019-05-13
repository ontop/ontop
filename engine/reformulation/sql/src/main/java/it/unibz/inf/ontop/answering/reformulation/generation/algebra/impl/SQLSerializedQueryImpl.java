package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLSerializedQuery;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * See SQLAlgebraFactory for creating a new instance.
 */
public class SQLSerializedQueryImpl implements SQLSerializedQuery {

    private final String sqlQueryString;
    private final ImmutableMap<Variable, String> columnNames;

    @AssistedInject
    private SQLSerializedQueryImpl(@Assisted String sqlString, @Assisted ImmutableMap<Variable, String> columnNames) {
        this.sqlQueryString = sqlString;
        this.columnNames = columnNames;
    }

    @Override
    public String getSQLString() {
        return sqlQueryString;
    }

    @Override
    public ImmutableMap<Variable, String> getColumnNames() {
        return columnNames;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
