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

    private static final QuestTypeMapper typeMap = new QuestTypeMapper();

    private final Map<Integer, Predicate.COL_TYPE> codeToTypeMap= new HashMap<Integer, Predicate.COL_TYPE>();;

    private final Map<Predicate.COL_TYPE, Integer> typeToCodeMap = new HashMap<Predicate.COL_TYPE, Integer>();

    private QuestTypeMapper() {

        typeToCodeMap.put(COL_TYPE.NULL, 0);
        typeToCodeMap.put(COL_TYPE.OBJECT, 1);
        typeToCodeMap.put(COL_TYPE.BNODE, 2);
        typeToCodeMap.put(COL_TYPE.LITERAL, 3);
        typeToCodeMap.put(COL_TYPE.INTEGER, 4);
        typeToCodeMap.put(COL_TYPE.DECIMAL, 5);
        typeToCodeMap.put(COL_TYPE.DOUBLE, 6);
        typeToCodeMap.put(COL_TYPE.STRING, 7);
        typeToCodeMap.put(COL_TYPE.DATETIME, 8);
        typeToCodeMap.put(COL_TYPE.BOOLEAN, 9);
        typeToCodeMap.put(COL_TYPE.DATE, 10);
        typeToCodeMap.put(COL_TYPE.TIME, 11);
        typeToCodeMap.put(COL_TYPE.YEAR, 12);
        typeToCodeMap.put(COL_TYPE.LONG, 13);
        typeToCodeMap.put(COL_TYPE.FLOAT, 14);
        typeToCodeMap.put(COL_TYPE.NEGATIVE_INTEGER, 15);
        typeToCodeMap.put(COL_TYPE.NON_NEGATIVE_INTEGER, 16);
        typeToCodeMap.put(COL_TYPE.POSITIVE_INTEGER, 17);
        typeToCodeMap.put(COL_TYPE.NON_POSITIVE_INTEGER, 18);
        typeToCodeMap.put(COL_TYPE.INT, 19);
        typeToCodeMap.put(COL_TYPE.UNSIGNED_INT, 20);


        codeToTypeMap.put(0, COL_TYPE.NULL);
        codeToTypeMap.put(1, COL_TYPE.OBJECT);
        codeToTypeMap.put(2, COL_TYPE.BNODE);
        codeToTypeMap.put(3, COL_TYPE.LITERAL);
        codeToTypeMap.put(4, COL_TYPE.INTEGER);
        codeToTypeMap.put(5, COL_TYPE.DECIMAL);
        codeToTypeMap.put(6, COL_TYPE.DOUBLE);
        codeToTypeMap.put(7, COL_TYPE.STRING);
        codeToTypeMap.put(8, COL_TYPE.DATETIME);
        codeToTypeMap.put(9, COL_TYPE.BOOLEAN);
        codeToTypeMap.put(10, COL_TYPE.DATE);
        codeToTypeMap.put(11, COL_TYPE.TIME);
        codeToTypeMap.put(12, COL_TYPE.YEAR);
        codeToTypeMap.put(13, COL_TYPE.LONG);
        codeToTypeMap.put(14, COL_TYPE.FLOAT);
        codeToTypeMap.put(15, COL_TYPE.NEGATIVE_INTEGER);
        codeToTypeMap.put(16, COL_TYPE.NON_NEGATIVE_INTEGER);
        codeToTypeMap.put(17, COL_TYPE.POSITIVE_INTEGER);
        codeToTypeMap.put(18, COL_TYPE.NON_POSITIVE_INTEGER);
        codeToTypeMap.put(19, COL_TYPE.INT);
        codeToTypeMap.put(20, COL_TYPE.UNSIGNED_INT);



    }

    public static QuestTypeMapper getInstance() {
        return typeMap;
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
