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

import it.unibz.inf.ontop.answering.resultset.OntopBindingSet;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.sql.ResultSet;
import java.util.*;

/**
 * Class to handle distinct in SPARQL query. Avoid returning duplicate rows.
 * See test case DistinctResultSetTest
 */

public class SQLDistinctTupleResultSet implements TupleResultSet {

    private SQLTupleResultSet tupleResultSet;

    private Set<List<Object>> distinctKeys;

    public SQLDistinctTupleResultSet(ResultSet set, List<String> signature,
                                     DBMetadata dbMetadata,
                                     Optional<IRIDictionary> iriDictionary, TermFactory termFactory,
                                     TypeFactory typeFactory) {

        tupleResultSet = new SQLTupleResultSet(set, signature, dbMetadata, iriDictionary, termFactory, typeFactory);

        distinctKeys = new HashSet<>();

    }


    @Override
    public int getColumnCount() {
        return tupleResultSet.getColumnCount();
    }

    @Override
    public List<String> getSignature() {
        return tupleResultSet.getSignature();
    }

    @Override
    public int getFetchSize() throws OntopConnectionException {
        return tupleResultSet.getFetchSize();
    }

    @Override
    public OntopBindingSet next() throws OntopConnectionException {
        return tupleResultSet.next();
    }

    @Override
    public void close() throws OntopConnectionException {
        distinctKeys.clear();
        tupleResultSet.close();

    }

    @Override
    public boolean hasNext() throws OntopConnectionException {
        // return the row only if it is not a duplicate

        boolean next = false;
        
        List<Object> row = null; 
        do{
            next = tupleResultSet.hasNext();
            if (next) {
                row = new ArrayList<>();
                for (int i = 1; i <= getSignature().size();  i ++ ) {
                    
                    int column = i * 3;
                    row.add(tupleResultSet.getRawObject(column-2));  //type
                    row.add(tupleResultSet.getRawObject(column-1)); //lang
                    row.add(tupleResultSet.getRawObject(column)); //value
                    
                }
            }
            else{
                distinctKeys.clear();
                break;
            }
        }while( !distinctKeys.add(row) );
        
        return next;
    }



//    @Override
//    public Constant getConstant(int column) throws OntopConnectionException, OntopResultConversionException {
//        return tupleResultSet.getConstant(column);
//    }
//
//    @Override
//    public Constant getConstant(String name) throws OntopConnectionException, OntopResultConversionException {
//        return tupleResultSet.getConstant(name);
//    }

}
