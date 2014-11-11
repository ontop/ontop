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
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.sql.Types;
import java.util.HashMap;

/**
 * This class maps SQL datatypes to XML datatypes.
 * Details look at this link: https://docs.google.com/spreadsheet/ccc?key=0AoDXwrYLJ2lvdFAtRXBPcUs2UjMtY08tZ3NYTEp5dVE&usp=sharing
 */
public class TypeMapper {

	private static final TypeMapper typeMapper;

	private final HashMap<Integer, Predicate> sqlToQuest = new HashMap<Integer, Predicate>();

	private static final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
	
	static {
		typeMapper = new TypeMapper();
		typeMapper.put(Types.VARCHAR, dtfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.CHAR, dtfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.LONGNVARCHAR, dtfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.LONGVARCHAR, dtfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.NVARCHAR, dtfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.NCHAR, dtfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.INTEGER, dtfac.getDataTypePredicateInteger());
		typeMapper.put(Types.BIGINT, dtfac.getDataTypePredicateInteger());
		typeMapper.put(Types.SMALLINT, dtfac.getDataTypePredicateInteger());
		typeMapper.put(Types.TINYINT, dtfac.getDataTypePredicateInteger());
		typeMapper.put(Types.NUMERIC, dtfac.getDataTypePredicateDecimal());
		typeMapper.put(Types.DECIMAL, dtfac.getDataTypePredicateDecimal());
		typeMapper.put(Types.FLOAT, dtfac.getDataTypePredicateDouble());
		typeMapper.put(Types.DOUBLE, dtfac.getDataTypePredicateDouble());
		typeMapper.put(Types.REAL, dtfac.getDataTypePredicateDouble());
		typeMapper.put(Types.DATE, dtfac.getDataTypePredicateDate());
		typeMapper.put(Types.TIME, dtfac.getDataTypePredicateTime());
		typeMapper.put(Types.TIMESTAMP, dtfac.getDataTypePredicateDateTime());
		typeMapper.put(Types.BOOLEAN, dtfac.getDataTypePredicateBoolean());
		typeMapper.put(Types.BIT, dtfac.getDataTypePredicateBoolean());
//		typeMapper.put(Types.BINARY, dfac.getDataTypePredicateBinary());
//		typeMapper.put(Types.VARBINARY, dfac.getDataTypePredicateBinary());
//		typeMapper.put(Types.BLOB, dfac.getDataTypePredicateBinary());
		typeMapper.put(Types.CLOB, dtfac.getDataTypePredicateLiteral());
		typeMapper.put(Types.OTHER, dtfac.getDataTypePredicateLiteral());
	}

	public static TypeMapper getInstance() {
		return typeMapper;
	}

	private void put(int sqlType, Predicate questType) {
		sqlToQuest.put(sqlType, questType);
	}
	
	public Predicate getPredicate(int sqlType) {
		Predicate result = sqlToQuest.get(sqlType);
		if (result == null) {
			result = dtfac.getDataTypePredicateLiteral();
		}
		return result;
	}
}
