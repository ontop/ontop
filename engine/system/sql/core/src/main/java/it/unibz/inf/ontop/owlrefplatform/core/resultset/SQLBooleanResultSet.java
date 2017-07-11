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
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ValueConstant;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;

public class SQLBooleanResultSet implements BooleanResultSet {

    private ResultSet set = null;
    private boolean isTrue = false;
    private int counter = 0;

    private ValueConstant valueConstant;

    public SQLBooleanResultSet(ResultSet set) {
        this.set = set;
        try {
            isTrue = set.next();
            valueConstant = DATA_FACTORY.getBooleanConstant(isTrue);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public SQLBooleanResultSet(boolean value) {
        this.set = null;
        isTrue = value;
    }

    @Override
    public void close() throws OntopConnectionException {
        if (set == null)
            return;
        try {
            set.close();
        } catch (Exception e) {
            throw new OntopConnectionException(e);
        }
    }

    /**
     * returns always 1
     */
    @Override
    public int getColumnCount() {
        return 1;
    }

    /**
     * returns the current fetch size. the default value is 100
     */
    @Override
    public int getFetchSize() {
        return 100;
    }

    /*
     * TODO: GUOHUI (2016-01-09) Understand what should be the right behavior
     */
    @Override
    public List<String> getSignature() throws OntopConnectionException {
        Vector<String> signature = new Vector<String>();
        if (set != null) {
            int i = getColumnCount();

            for (int j = 1; j <= i; j++) {
                try {
                    signature.add(set.getMetaData().getColumnLabel(j));
                } catch (Exception e) {
                    throw new OntopConnectionException(e.getMessage());
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
    public boolean nextRow() {
        if (!isTrue || counter > 0) {
            return false;
        } else {
            counter++;
            return true;
        }
    }

    @Override
    public Constant getConstant(int column) {

        return valueConstant;
    }

    @Override
    public Constant getConstant(String name) {
        return valueConstant;
    }


}
