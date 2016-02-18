package it.unibz.inf.ontop.utils;

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

import it.unibz.inf.ontop.model.Predicate;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * This class maps SQL datatypes to XML datatypes.
 * Details look at this link: https://docs.google.com/spreadsheet/ccc?key=0AoDXwrYLJ2lvdFAtRXBPcUs2UjMtY08tZ3NYTEp5dVE&usp=sharing
 */
public class JdbcTypeMapper {

	private final Map<Integer, Predicate.COL_TYPE> sqlToQuest = new HashMap<Integer, Predicate.COL_TYPE>();
	private final Map<Predicate.COL_TYPE, Integer> datatypeMap = new HashMap<Predicate.COL_TYPE, Integer>();
	
	public JdbcTypeMapper() {
		sqlToQuest.put(Types.VARCHAR, Predicate.COL_TYPE.LITERAL);
		sqlToQuest.put(Types.CHAR, Predicate.COL_TYPE.LITERAL);
		sqlToQuest.put(Types.LONGNVARCHAR, Predicate.COL_TYPE.LITERAL);
		sqlToQuest.put(Types.LONGVARCHAR, Predicate.COL_TYPE.LITERAL);
		sqlToQuest.put(Types.NVARCHAR, Predicate.COL_TYPE.LITERAL);
		sqlToQuest.put(Types.NCHAR, Predicate.COL_TYPE.LITERAL);
		sqlToQuest.put(Types.INTEGER, Predicate.COL_TYPE.INTEGER);
		sqlToQuest.put(Types.BIGINT, Predicate.COL_TYPE.INTEGER);
		sqlToQuest.put(Types.SMALLINT, Predicate.COL_TYPE.INTEGER);
		sqlToQuest.put(Types.TINYINT, Predicate.COL_TYPE.INTEGER);
		sqlToQuest.put(Types.NUMERIC, Predicate.COL_TYPE.DECIMAL);
		sqlToQuest.put(Types.DECIMAL, Predicate.COL_TYPE.DECIMAL);
		sqlToQuest.put(Types.FLOAT, Predicate.COL_TYPE.DOUBLE); // R: why not FLOAT?
		sqlToQuest.put(Types.DOUBLE, Predicate.COL_TYPE.DOUBLE);
		sqlToQuest.put(Types.REAL, Predicate.COL_TYPE.DOUBLE);
		sqlToQuest.put(Types.DATE, Predicate.COL_TYPE.DATE);
		sqlToQuest.put(Types.TIME, Predicate.COL_TYPE.TIME);
		sqlToQuest.put(Types.TIMESTAMP, Predicate.COL_TYPE.DATETIME); //GX: needs check
		sqlToQuest.put(Types.BOOLEAN, Predicate.COL_TYPE.BOOLEAN);
		sqlToQuest.put(Types.BIT, Predicate.COL_TYPE.BOOLEAN);
//		typeMapper.put(Types.BINARY, dfac.getDataTypePredicateBinary());
//		typeMapper.put(Types.VARBINARY, dfac.getDataTypePredicateBinary());
//		typeMapper.put(Types.BLOB, dfac.getDataTypePredicateBinary());
		sqlToQuest.put(Types.CLOB, Predicate.COL_TYPE.LITERAL);
		sqlToQuest.put(Types.OTHER, Predicate.COL_TYPE.LITERAL);
		
		datatypeMap.put(Predicate.COL_TYPE.BOOLEAN, Types.BOOLEAN);
		datatypeMap.put(Predicate.COL_TYPE.INT, Types.INTEGER);
		datatypeMap.put(Predicate.COL_TYPE.INTEGER, Types.BIGINT);
		datatypeMap.put(Predicate.COL_TYPE.LONG, Types.BIGINT);
		datatypeMap.put(Predicate.COL_TYPE.NEGATIVE_INTEGER, Types.BIGINT);
		datatypeMap.put(Predicate.COL_TYPE.POSITIVE_INTEGER, Types.BIGINT);
		datatypeMap.put(Predicate.COL_TYPE.NON_POSITIVE_INTEGER, Types.BIGINT);
		datatypeMap.put(Predicate.COL_TYPE.UNSIGNED_INT, Types.INTEGER);
		datatypeMap.put(Predicate.COL_TYPE.FLOAT, Types.FLOAT);
		datatypeMap.put(Predicate.COL_TYPE.DOUBLE, Types.DOUBLE);
		datatypeMap.put(Predicate.COL_TYPE.STRING, Types.VARCHAR);
		datatypeMap.put(Predicate.COL_TYPE.LITERAL, Types.VARCHAR);
		datatypeMap.put(Predicate.COL_TYPE.DATETIME_STAMP, Types.TIMESTAMP);
        datatypeMap.put(Predicate.COL_TYPE.DECIMAL, Types.DECIMAL);
		// all other COL_TYPEs are mapped to Types.VARCHAR by default
	}
	
	public Predicate.COL_TYPE getPredicate(int sqlType) {
		Predicate.COL_TYPE type = sqlToQuest.get(sqlType);
		if (type == null) 
			type = Predicate.COL_TYPE.LITERAL;
		
		return type;
	}
	
	public int getSQLType(Predicate.COL_TYPE type) {
		if (type != null) {
			Integer sqlType = datatypeMap.get(type);
			if (sqlType != null)
				return sqlType;
		}
		// Return varchar for unknown
		return Types.VARCHAR;
	}
	
}
