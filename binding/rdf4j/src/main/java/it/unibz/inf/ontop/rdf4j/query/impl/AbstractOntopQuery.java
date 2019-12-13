/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package it.unibz.inf.ontop.rdf4j.query.impl;

import it.unibz.inf.ontop.answering.connection.OntopConnection;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

/**
 * TODO: get rid of the query string and keeps the bindings separated from the ParsedQuery
 */
public abstract class AbstractOntopQuery implements Query {

    /**
     * TODO: remove the query string (when having a proper support of bindings)
     */
    private final String queryString;
    private final ParsedQuery initialParsedQuery;
    private final String baseIRI;
    protected final OntopConnection conn;
    protected int queryTimeout;
    protected MapBindingSet bindings = new MapBindingSet();

    protected AbstractOntopQuery(String queryString, String baseIRI,
                                 ParsedQuery initialParsedQuery, OntopConnection conn) {
        this.queryString = queryString;
        this.baseIRI = baseIRI;
        this.conn = conn;
        this.queryTimeout = 0;
        this.initialParsedQuery = initialParsedQuery;
    }

    @Override
    public void setBinding(String s, Value value) {
        bindings.addBinding(s, value);
    }

    @Override
    public void removeBinding(String s) {
        bindings.removeBinding(s);
    }

    @Override
    public void clearBindings() {
        bindings.clear();
    }

    @Override
    public BindingSet getBindings() {
        return bindings;
    }

    @Override
    public Dataset getDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDataset(Dataset dataset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getIncludeInferred() {
        return true;
    }

    /**
     * Always true.
     */
    @Override
    public void setIncludeInferred(boolean includeInferred) {
        if (!includeInferred)
            throw new UnsupportedOperationException("Inference can't be disabled.");
    }

    @Override
    public int getMaxQueryTime() {
        return this.queryTimeout;
    }

    @Override
    public void setMaxQueryTime(int maxQueryTime) {
        this.queryTimeout = maxQueryTime;
    }

    @Override
    public void setMaxExecutionTime(int maxExecTime) {
        setMaxQueryTime(maxExecTime);
    }

    @Override
    public int getMaxExecutionTime() {
        return getMaxQueryTime();
    }

    //all code below is copy-pasted from org.eclipse.rdf4j.repository.sparql.query.SPARQLOperation
    protected String getQueryString() {
        if (bindings.size() == 0)
            return queryString;
        String qry = queryString;
        int b = qry.indexOf('{');
        String select = qry.substring(0, b);
        String where = qry.substring(b);
        for (String name : bindings.getBindingNames()) {
            String replacement = getReplacement(bindings.getValue(name));
            if (replacement != null) {
                String pattern = "[\\?\\$]" + name + "(?=\\W)";
                select = select.replaceAll(pattern, "");
                where = where.replaceAll(pattern, replacement);
            }
        }
        return select + where;
    }

    protected ParsedQuery getParsedQuery() {
        // NB: no binding at construction time
        if (bindings.size() == 0)
            return initialParsedQuery;
        else {
            return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, getQueryString(), baseIRI);
        }
    }


    private String getReplacement(Value value) {
        StringBuilder sb = new StringBuilder();
        if (value instanceof IRI) {
            return appendValue(sb, (IRI) value).toString();
        } else if (value instanceof Literal) {
            return appendValue(sb, (Literal) value).toString();
        } else {
            throw new IllegalArgumentException(
                    "BNode references not supported by SPARQL end-points");
        }
    }

    private StringBuilder appendValue(StringBuilder sb, IRI uri) {
        sb.append("<").append(uri.stringValue()).append(">");
        return sb;
    }

    private StringBuilder appendValue(StringBuilder sb, Literal lit) {
        sb.append('"');
        sb.append(lit.getLabel().replace("\"", "\\\""));
        sb.append('"');

        if (lit.getLanguage() != null) {
            sb.append('@');
            sb.append(lit.getLanguage());
        }
        else if (lit.getDatatype() != null) {
            sb.append("^^<");
            sb.append(lit.getDatatype().stringValue());
            sb.append('>');
        }
        return sb;
    }
}