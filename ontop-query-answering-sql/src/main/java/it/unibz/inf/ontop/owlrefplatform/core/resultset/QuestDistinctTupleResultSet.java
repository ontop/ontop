package it.unibz.inf.ontop.owlrefplatform.core.resultset;

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

import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.owlrefplatform.core.QuestStatement;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;

import java.sql.ResultSet;
import java.util.*;

/**
 * Class to handle distinct in SPARQL query. Avoid returning duplicate rows.
 * See test case DistinctResultSetTest
 */

public class QuestDistinctTupleResultSet implements TupleResultSet {

    private QuestTupleResultSet questTupleResultSet;

    private Set<List<Object>> distinctKeys;

    public QuestDistinctTupleResultSet(ResultSet set, List<String> signature, QuestStatement st,
                                       DBMetadata dbMetadata,
                                       Optional<IRIDictionary> iriDictionary) {

        questTupleResultSet = new QuestTupleResultSet(set, signature, st, dbMetadata, iriDictionary);

        distinctKeys = new HashSet<>();

    }


    @Override
    public int getColumnCount() {
        return questTupleResultSet.getColumnCount();
    }

    @Override
    public List<String> getSignature() {
        return questTupleResultSet.getSignature();
    }

    @Override
    public int getFetchSize() throws OntopConnectionException {
        return questTupleResultSet.getFetchSize();
    }

    @Override
    public void close() throws OntopConnectionException {
        distinctKeys.clear();
        questTupleResultSet.close();

    }

    @Override
    public OBDAStatement getStatement() {
        return questTupleResultSet.getStatement();
    }

    @Override
    public boolean nextRow() throws OntopConnectionException {
        // return the row only if it is not a duplicate

        boolean next = false;
        
        List<Object> row = null; 
        do{
            next = questTupleResultSet.nextRow();
            if (next) {
                row = new ArrayList<>();
                for (int i = 1; i <= getSignature().size();  i ++ ) {
                    
                    int column = i * 3;
                    row.add(questTupleResultSet.getRawObject(column-2));  //type
                    row.add(questTupleResultSet.getRawObject(column-1)); //lang
                    row.add(questTupleResultSet.getRawObject(column)); //value
                    
                }
            }
            else{
                distinctKeys.clear();
                break;
            }
        }while( !distinctKeys.add(row) );
        
        return next;
    }

    @Override
    public Constant getConstant(int column) throws OntopConnectionException, OntopResultConversionException {
        return questTupleResultSet.getConstant(column);
    }

    @Override
    public Constant getConstant(String name) throws OntopConnectionException, OntopResultConversionException {
        return questTupleResultSet.getConstant(name);
    }

}
