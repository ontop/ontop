package it.unibz.krdb.obda.model.impl;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import java.util.HashMap;
import java.util.Map;

public class QuestTypeMapper {

    private final Map<Integer, Predicate.COL_TYPE> codeToTypeMap= new HashMap<Integer, Predicate.COL_TYPE>();
    private final Map<Predicate.COL_TYPE, Integer> typeToCodeMap = new HashMap<Predicate.COL_TYPE, Integer>();

    QuestTypeMapper() {
        regsiterType(COL_TYPE.NULL, 0);
        regsiterType(COL_TYPE.OBJECT, 1);
        regsiterType(COL_TYPE.BNODE, 2);
        regsiterType(COL_TYPE.LITERAL, 3);
        regsiterType(COL_TYPE.INTEGER, 4);
        regsiterType(COL_TYPE.DECIMAL, 5);
        regsiterType(COL_TYPE.DOUBLE, 6);
        regsiterType(COL_TYPE.STRING, 7);
        regsiterType(COL_TYPE.DATETIME, 8);
        regsiterType(COL_TYPE.BOOLEAN, 9);
        regsiterType(COL_TYPE.DATE, 10);
        regsiterType(COL_TYPE.TIME, 11);
        regsiterType(COL_TYPE.YEAR, 12);
        regsiterType(COL_TYPE.LONG, 13);
        regsiterType(COL_TYPE.FLOAT, 14);
        regsiterType(COL_TYPE.NEGATIVE_INTEGER, 15);
        regsiterType(COL_TYPE.NON_NEGATIVE_INTEGER, 16);
        regsiterType(COL_TYPE.POSITIVE_INTEGER, 17);
        regsiterType(COL_TYPE.NON_POSITIVE_INTEGER, 18);
        regsiterType(COL_TYPE.INT, 19);
        regsiterType(COL_TYPE.UNSIGNED_INT, 20);
    }

    private final void regsiterType(COL_TYPE type, int questCode) {
        typeToCodeMap.put(type, questCode);
        codeToTypeMap.put(questCode, type);
    }
    
    public Integer getQuestCode(COL_TYPE type) {
        Integer result = typeToCodeMap.get(type);
        return result;
    }

    public COL_TYPE getQuestType(int questCode) {
        COL_TYPE sqlType = codeToTypeMap.get(questCode);
        return sqlType;
    }
}
