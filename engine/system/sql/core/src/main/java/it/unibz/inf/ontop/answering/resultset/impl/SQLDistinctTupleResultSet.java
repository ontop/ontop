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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Class to handle distinct in SPARQL query. Avoid returning duplicate rows.
 * See test case DistinctResultSetTest
 */

public class SQLDistinctTupleResultSet extends DelegatedIriSQLTupleResultSet implements TupleResultSet {

    private Set<List<Object>> rowKeys;

    public SQLDistinctTupleResultSet(ResultSet rs, ImmutableList<String> signature,
                                     DBMetadata dbMetadata,
                                     Optional<IRIDictionary> iriDictionary, TermFactory termFactory,
                                     TypeFactory typeFactory, RDF rdfFactory) {

        super(rs, signature, dbMetadata, iriDictionary, termFactory, typeFactory, rdfFactory);
        rowKeys = new HashSet<>();
    }

    /**
     * Moves cursor until we get a fresh row
     */
    @Override
    protected boolean moveCursor() throws SQLException, OntopConnectionException {
        boolean foundFreshRow;
        List<Object> currentKey;
        do{
           foundFreshRow = rs.next();
           // Cannot use this in the while condition: limit case where the last row was a duplicate
           if(!foundFreshRow) {
               break;
           }
           currentKey = computeRowKey(rs);
        } while(!rowKeys.add(currentKey));

        return foundFreshRow;
    }

    private List<Object> computeRowKey(ResultSet rs) throws OntopConnectionException {

        ArrayList rowKey = new ArrayList<>();
        for (int i = 1; i <= getSignature().size();  i ++ ) {
            int column = i * 3;
            rowKey.add(getRawObject(column-2));  //type
            rowKey.add(getRawObject(column-1)); //lang
            rowKey.add(getRawObject(column)); //value
        }
        return rowKey;
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
