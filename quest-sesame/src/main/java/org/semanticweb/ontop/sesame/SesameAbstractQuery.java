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
package org.semanticweb.ontop.sesame;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Query;
import org.openrdf.query.impl.MapBindingSet;
import org.semanticweb.ontop.owlrefplatform.core.QuestDBConnection;

public abstract class SesameAbstractQuery implements Query {

    protected final String queryString;
    protected final QuestDBConnection conn;
    protected int queryTimeout;
    protected MapBindingSet bindings = new MapBindingSet();

    protected SesameAbstractQuery(String queryString, QuestDBConnection conn) {
        this.queryString = queryString;
        this.conn = conn;
        this.queryTimeout = 0;
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
    public void setIncludeInferred(boolean b) {
        if (b == false)
            throw new IllegalArgumentException("Inference can't be disabled.");
    }

    @Override
    public int getMaxQueryTime() {
        return this.queryTimeout;
    }

    @Override
    public void setMaxQueryTime(int maxQueryTime) {
        this.queryTimeout = maxQueryTime;
    }

    //all code below is copy-pasted from org.openrdf.repository.sparql.query.SPARQLOperation
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

    private String getReplacement(Value value) {
        StringBuilder sb = new StringBuilder();
        if (value instanceof URI) {
            return appendValue(sb, (URI) value).toString();
        } else if (value instanceof Literal) {
            return appendValue(sb, (Literal) value).toString();
        } else {
            throw new IllegalArgumentException(
                    "BNode references not supported by SPARQL end-points");
        }
    }

    private StringBuilder appendValue(StringBuilder sb, URI uri) {
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