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

    private QuestResultset questResultset;



    private Set<List<Object>> distinctKeys = new HashSet<List<Object>>();

    public QuestDistinctResultset(ResultSet set, List<String> signature, QuestStatement st) throws OBDAException {

        questResultset = new QuestResultset(set, signature, st);

    }


    @Override
    public int getColumnCount() throws OBDAException {
        return questResultset.getColumnCount();
    }

    @Override
    public List<String> getSignature() throws OBDAException {
        return questResultset.getSignature();
    }

    @Override
    public int getFetchSize() throws OBDAException {
        return questResultset.getFetchSize();
    }

    @Override
    public void close() throws OBDAException {
        questResultset.close();
    }

    @Override
    public OBDAStatement getStatement() {
        return questResultset.getStatement();
    }

    @Override
    public boolean nextRow() throws OBDAException {
        // return the row only if it is not a duplicate

        boolean next = questResultset.nextRow();

        if (next) {

            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= getSignature().size(); i = i * 3) {

                row.add(questResultset.getRawObject(i));  //type
                row.add(questResultset.getRawObject(i+1)); //lang
                row.add(questResultset.getRawObject(i+2)); //value

            }
            if (!distinctKeys.add(row)) {

                //serch for the next value since the current one is a duplicate
                return nextRow();
            }

        }
        return next;
    }

    @Override
    public Constant getConstant(int column) throws OBDAException {
        return questResultset.getConstant(column);
    }

    @Override
    public Constant getConstant(String name) throws OBDAException {
        return questResultset.getConstant(name);
    }

}
