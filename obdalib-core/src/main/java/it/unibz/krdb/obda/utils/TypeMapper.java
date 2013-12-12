package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.sql.Types;
import java.util.HashMap;

/**
 * This class maps SQL datatypes to XML datatypes.
 * Details look at this link: https://docs.google.com/spreadsheet/ccc?key=0AoDXwrYLJ2lvdFAtRXBPcUs2UjMtY08tZ3NYTEp5dVE&usp=sharing
 */
public class TypeMapper {

	private static TypeMapper typeMapper;

	private final HashMap<Integer, Predicate> sqlToQuest = new HashMap<Integer, Predicate>();

	private static OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	static {
		typeMapper = new TypeMapper();
		typeMapper.put(Types.VARCHAR, dfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.CHAR, dfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.LONGNVARCHAR, dfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.LONGVARCHAR, dfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.NVARCHAR, dfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.NCHAR, dfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.INTEGER, dfac.getDataTypePredicateInteger());
		typeMapper.put(Types.BIGINT, dfac.getDataTypePredicateInteger());
		typeMapper.put(Types.SMALLINT, dfac.getDataTypePredicateInteger());
		typeMapper.put(Types.TINYINT, dfac.getDataTypePredicateInteger());
		typeMapper.put(Types.NUMERIC, dfac.getDataTypePredicateDecimal());
		typeMapper.put(Types.DECIMAL, dfac.getDataTypePredicateDecimal());
		typeMapper.put(Types.FLOAT, dfac.getDataTypePredicateDouble());
		typeMapper.put(Types.DOUBLE, dfac.getDataTypePredicateDouble());
		typeMapper.put(Types.REAL, dfac.getDataTypePredicateDouble());
//		typeMapper.put(Types.DATE, dfac.getDataTypePredicateDate());
//		typeMapper.put(Types.TIME, dfac.getDataTypePredicateTime());
		typeMapper.put(Types.TIMESTAMP, dfac.getDataTypePredicateDateTime());
		typeMapper.put(Types.BOOLEAN, dfac.getDataTypePredicateBoolean());
		typeMapper.put(Types.BIT, dfac.getDataTypePredicateBoolean());
//		typeMapper.put(Types.BINARY, dfac.getDataTypePredicateBinary());
//		typeMapper.put(Types.VARBINARY, dfac.getDataTypePredicateBinary());
//		typeMapper.put(Types.BLOB, dfac.getDataTypePredicateBinary());
		typeMapper.put(Types.CLOB, dfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.OTHER, dfac.getDataTypePredicateLiteral());
	}

	public static TypeMapper getInstance() {
		return typeMapper;
	}

	public void put(int sqlType, Predicate questType) {
		sqlToQuest.put(sqlType, questType);
	}
	
	public Predicate getPredicate(int sqlType) {
		Predicate result = sqlToQuest.get(sqlType);
		if (result == null) {
			result = dfac.getDataTypePredicateLiteral();
		}
		return result;
	}
}
