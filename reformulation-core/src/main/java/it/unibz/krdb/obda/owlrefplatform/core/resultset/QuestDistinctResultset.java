package it.unibz.krdb.obda.owlrefplatform.core.resultset;

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

import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.TupleResultSet;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to handle distinct in SPARQL query. Avoid returning duplicate rows.
 * See test case DistinctResultSetTest
 */

public class QuestDistinctResultset implements TupleResultSet {

    private QuestResultSet questResultSet;



    private Set<List<Object>> distinctKeys;

    public QuestDistinctResultset(ResultSet set, List<String> signature, QuestStatement st) throws OBDAException {

        questResultSet = new QuestResultSet(set, signature, st);

        distinctKeys = new HashSet<List<Object>>();

    }


    @Override
    public int getColumnCount() throws OBDAException {
        return questResultSet.getColumnCount();
    }

    @Override
    public List<String> getSignature() throws OBDAException {
        return questResultSet.getSignature();
    }

    @Override
    public int getFetchSize() throws OBDAException {
        return questResultSet.getFetchSize();
    }

    @Override
    public void close() throws OBDAException {
        distinctKeys.clear();
        questResultSet.close();

    }

    @Override
    public OBDAStatement getStatement() {
        return questResultSet.getStatement();
    }

    @Override
    public boolean nextRow() throws OBDAException {
        // return the row only if it is not a duplicate

        boolean next = false;
        
        List<Object> row = null; 
        do{
            next = questResultSet.nextRow();
            if (next) {
                row = new ArrayList<>();
                for (int i = 1; i <= getSignature().size();  i ++ ) {
                    
                    int column = i * 3;
                    row.add(questResultSet.getRawObject(column-2));  //type
                    row.add(questResultSet.getRawObject(column-1)); //lang
                    row.add(questResultSet.getRawObject(column)); //value
                    
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
    public Constant getConstant(int column) throws OBDAException {
        return questResultSet.getConstant(column);
    }

    @Override
    public Constant getConstant(String name) throws OBDAException {
        return questResultSet.getConstant(name);
    }

}
