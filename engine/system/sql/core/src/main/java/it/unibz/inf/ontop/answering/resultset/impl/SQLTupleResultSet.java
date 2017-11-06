package it.unibz.inf.ontop.answering.resultset.impl;

/*
 * #%L
 * ontop-reformulation-core
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


import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SQLTupleResultSet implements TupleResultSet {

    private final ResultSet rs;

    private final List<String> signature;

    private final Map<String, Integer> columnMap;

    private final JDBC2ConstantConverter ontopConstantRetriever;


    /***
     * Constructs an OBDA statement from an SQL statement, a signature described
     * by terms and a statement. The statement is maintained only as a reference
     * for closing operations.
     * @param set
     * @param signature
     *            A list of terms that determines the type of the columns of
     * @param termFactory
     * @param typeFactory
     */
    public SQLTupleResultSet(ResultSet set, List<String> signature,
                             DBMetadata dbMetadata, Optional<IRIDictionary> iriDictionary,
                             TermFactory termFactory, TypeFactory typeFactory) {
        this.rs = set;

        this.signature = signature;

        columnMap = new HashMap<>(signature.size() * 2);

        for (int j = 1; j <= signature.size(); j++) {
            columnMap.put(signature.get(j - 1), j);
        }

        this.ontopConstantRetriever = new JDBC2ConstantConverter(dbMetadata, iriDictionary, termFactory, typeFactory);
    }

    @Override
    public int getColumnCount() {
        return signature.size();
    }

    @Override
    public int getFetchSize() throws OntopConnectionException {
        try {
            return rs.getFetchSize();
        } catch (Exception e) {
            throw new OntopConnectionException(e.getMessage());
        }
    }

    @Override
    public OntopBindingSet next() throws OntopConnectionException {
        SQLRowReader rowReader = new SQLRowReader();

        try {
            final List<MainTypeLangValues> cells = rowReader.read(rs, getColumnCount());
            return new SQLOntopBindingSet(cells, signature, columnMap, ontopConstantRetriever);
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }

    }

    @Override
    public List<String> getSignature() {
        return signature;
    }

    @Override
    public boolean hasNext() throws OntopConnectionException {
        try {
            // FIXME(xiao): don't call rs.next() twice when calling this.hasNext() twice
            return rs.next();
        } catch (Exception e) {
            throw new OntopConnectionException(e);
        }
    }

    @Override
    public void close() throws OntopConnectionException {
        try {
            rs.close();
        } catch (Exception e) {
            throw new OntopConnectionException(e);
        }
    }

    Object getRawObject(int column) throws OntopConnectionException {
        try {
            Object realValue = rs.getObject(column);
            return realValue;
        } catch (Exception e) {
            throw new OntopConnectionException(e);
        }
    }


}
