package it.unibz.inf.ontop.answering.reformulation.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;

/**
 * SQL-query string, signature and optional construct template
 * (for SPARQL CONSTRUCT queries).
 */
public class SQLExecutableQuery implements ExecutableQuery {

    private final String sqlQuery;
    private final ImmutableList<String> signature;

    public SQLExecutableQuery(String sqlQuery, ImmutableList<String> signature) {
        this.sqlQuery = sqlQuery;
        this.signature = signature;
    }
    /**
     * Empty SQL
     */
    public SQLExecutableQuery(ImmutableList<String> signature) {
        this.sqlQuery = "";
        this.signature = signature;
    }

    @Override
    public ImmutableList<String> getSignature() {
        return signature;
    }

    public String getSQL() {
        return sqlQuery;
    }

    @Override
    public String toString() {
        return sqlQuery;
    }
}
