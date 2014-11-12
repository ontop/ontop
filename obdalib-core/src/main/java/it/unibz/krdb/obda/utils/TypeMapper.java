package it.unibz.krdb.obda.utils;

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

import it.unibz.krdb.obda.model.DatatypeFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * This class maps SQL datatypes to XML datatypes.
 * Details look at this link: https://docs.google.com/spreadsheet/ccc?key=0AoDXwrYLJ2lvdFAtRXBPcUs2UjMtY08tZ3NYTEp5dVE&usp=sharing
 */
public class TypeMapper {

	private static final TypeMapper typeMapper = new TypeMapper();

	private final Map<Integer, Predicate> sqlToQuest = new HashMap<Integer, Predicate>();
	
	private final Map<Predicate.COL_TYPE, Integer> datatypeMap = new HashMap<Predicate.COL_TYPE, Integer>();
	
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
	

	private TypeMapper() {
		sqlToQuest.put(Types.VARCHAR, dtfac.getDataTypePredicateLiteral());
		sqlToQuest.put(Types.CHAR, dtfac.getDataTypePredicateLiteral());
		sqlToQuest.put(Types.LONGNVARCHAR, dtfac.getDataTypePredicateLiteral());
		sqlToQuest.put(Types.LONGVARCHAR, dtfac.getDataTypePredicateLiteral());
		sqlToQuest.put(Types.NVARCHAR, dtfac.getDataTypePredicateLiteral());
		sqlToQuest.put(Types.NCHAR, dtfac.getDataTypePredicateLiteral());
		sqlToQuest.put(Types.INTEGER, dtfac.getTypePredicate(COL_TYPE.INTEGER));
		sqlToQuest.put(Types.BIGINT, dtfac.getTypePredicate(COL_TYPE.INTEGER));
		sqlToQuest.put(Types.SMALLINT, dtfac.getTypePredicate(COL_TYPE.INTEGER));
		sqlToQuest.put(Types.TINYINT, dtfac.getTypePredicate(COL_TYPE.INTEGER));
		sqlToQuest.put(Types.NUMERIC, dtfac.getTypePredicate(COL_TYPE.DECIMAL));
		sqlToQuest.put(Types.DECIMAL, dtfac.getTypePredicate(COL_TYPE.DECIMAL));
		sqlToQuest.put(Types.FLOAT, dtfac.getTypePredicate(COL_TYPE.DOUBLE)); // R: why not FLOAT?
		sqlToQuest.put(Types.DOUBLE, dtfac.getTypePredicate(COL_TYPE.DOUBLE));
		sqlToQuest.put(Types.REAL, dtfac.getTypePredicate(COL_TYPE.DOUBLE));
		sqlToQuest.put(Types.DATE, dtfac.getTypePredicate(COL_TYPE.DATE));
		sqlToQuest.put(Types.TIME, dtfac.getTypePredicate(COL_TYPE.TIME));
		sqlToQuest.put(Types.TIMESTAMP, dtfac.getTypePredicate(COL_TYPE.DATETIME));
		sqlToQuest.put(Types.BOOLEAN, dtfac.getTypePredicate(COL_TYPE.BOOLEAN));
		sqlToQuest.put(Types.BIT, dtfac.getTypePredicate(COL_TYPE.BOOLEAN));
//		typeMapper.put(Types.BINARY, dfac.getDataTypePredicateBinary());
//		typeMapper.put(Types.VARBINARY, dfac.getDataTypePredicateBinary());
//		typeMapper.put(Types.BLOB, dfac.getDataTypePredicateBinary());
		sqlToQuest.put(Types.CLOB, dtfac.getDataTypePredicateLiteral());
		sqlToQuest.put(Types.OTHER, dtfac.getDataTypePredicateLiteral());		
		
		datatypeMap.put(COL_TYPE.BOOLEAN, Types.BOOLEAN);
		datatypeMap.put(COL_TYPE.INT, Types.INTEGER);
		datatypeMap.put(COL_TYPE.INTEGER, Types.BIGINT);
		datatypeMap.put(COL_TYPE.LONG, Types.BIGINT);
		datatypeMap.put(COL_TYPE.NEGATIVE_INTEGER, Types.BIGINT);
		datatypeMap.put(COL_TYPE.POSITIVE_INTEGER, Types.BIGINT);
		datatypeMap.put(COL_TYPE.NON_POSITIVE_INTEGER, Types.BIGINT);
		datatypeMap.put(COL_TYPE.UNSIGNED_INT, Types.INTEGER);
		datatypeMap.put(COL_TYPE.FLOAT, Types.FLOAT);
		datatypeMap.put(COL_TYPE.DOUBLE, Types.DOUBLE);
		datatypeMap.put(COL_TYPE.STRING, Types.VARCHAR);
		datatypeMap.put(COL_TYPE.LITERAL, Types.VARCHAR);	
	}
	
	public static TypeMapper getInstance() {
		return typeMapper;
	}

	public Predicate getPredicate(int sqlType) {
		Predicate result = sqlToQuest.get(sqlType);
		if (result == null) {
			result = dtfac.getDataTypePredicateLiteral();
		}
		return result;
	}
	
	public int getSQLType(Predicate p) {
		Predicate.COL_TYPE type = dtfac.getDataType(p.toString());
		if (type != null) {
			Integer sqlType = datatypeMap.get(type);
			if (sqlType != null)
				return sqlType;
		}
		// Return varchar for unknown
		return Types.VARCHAR;
	}
	
}
