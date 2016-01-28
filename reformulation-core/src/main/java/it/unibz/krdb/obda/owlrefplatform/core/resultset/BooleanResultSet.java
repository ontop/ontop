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

import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class BooleanResultSet implements TupleResultSet {

    private ResultSet set = null;
    private boolean isTrue = false;
    private int counter = 0;
    private OBDAStatement st;

    private ValueConstant valueConstant;

    public BooleanResultSet(ResultSet set, OBDAStatement st) {
        this.set = set;
        this.st = st;
        try {
            isTrue = set.next();
            OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
            valueConstant = fac.getBooleanConstant(isTrue);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public BooleanResultSet(boolean value, OBDAStatement st) {
        this.set = null;
        this.st = st;
        isTrue = value;
    }

    @Override
    public void close() throws OBDAException {
        if (set == null)
            return;
        try {
            set.close();
        } catch (SQLException e) {
            throw new OBDAException(e.getMessage());
        }
    }

    /**
     * returns always 1
     */
    @Override
    public int getColumnCount() throws OBDAException {
        return 1;
    }

    /**
     * returns the current fetch size. the default value is 100
     */
    @Override
    public int getFetchSize() throws OBDAException {
        return 100;
    }

    /*
     * TODO: GUOHUI (2016-01-09) Understand what should be the right behavior
     */
    @Override
    public List<String> getSignature() throws OBDAException {
        Vector<String> signature = new Vector<String>();
        if (set != null) {
            int i = getColumnCount();

            for (int j = 1; j <= i; j++) {
                try {
                    signature.add(set.getMetaData().getColumnLabel(j));
                } catch (SQLException e) {
                    throw new OBDAException(e.getMessage());
                }
            }
        } else {
            signature.add("value");
        }
        return signature;
    }

    /**
     * Note: the boolean result set has only 1 row
     *
     * TODO: GUOHUI (2016-01-09) Understand what should be the right behavior
     */
    @Override
    public boolean nextRow() throws OBDAException {
        if (!isTrue || counter > 0) {
            return false;
        } else {
            counter++;
            return true;
        }
    }

    @Override
    public OBDAStatement getStatement() {
        return st;
    }

    @Override
    public Constant getConstant(int column) throws OBDAException {

        return valueConstant;
    }

    @Override
    public Constant getConstant(String name) throws OBDAException {
        return valueConstant;
    }


}
