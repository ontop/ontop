package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.owlrefplatform.core.translator.SesameConstructTemplate;

/**
 * SQL-query string, signature and optional construct template
 * (for SPARQL CONSTRUCT queries).
 */
public class SQLNativeQuery implements NativeQuery {

    private final String sqlQuery;
    private final ImmutableList<String> signature;
    private final Optional<SesameConstructTemplate> optionalConstructTemplate;

    public SQLNativeQuery(String sqlQuery, ImmutableList<String> signature,
                          Optional<SesameConstructTemplate> optionalConstructTemplate) {
        this.sqlQuery = sqlQuery;
        this.signature = signature;
        this.optionalConstructTemplate = optionalConstructTemplate;
    }

    public SQLNativeQuery(String sqlQuery, ImmutableList<String> signature) {
        this(sqlQuery, signature, Optional.<SesameConstructTemplate>absent());
    }

    /**
     * Empty SQL
     */
    public SQLNativeQuery(ImmutableList<String> signature, Optional<SesameConstructTemplate> optionalConstructTemplate) {
        this.sqlQuery = "";
        this.signature = signature;
        this.optionalConstructTemplate = optionalConstructTemplate;
    }

    @Override
    public Optional<SesameConstructTemplate> getOptionalConstructTemplate() {
        return optionalConstructTemplate;
    }

    @Override
    public boolean isEmpty() {
        return sqlQuery.isEmpty();
    }

    @Override
    public ImmutableList<String> getSignature() {
        return signature;
    }

    public String getSQL() {
        return sqlQuery;
    }
}
