package it.unibz.inf.ontop.dbschema;

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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.sql.Types;
import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

/**
 * This class maps SQL datatypes to XML datatypes.
 * Details look at this link: https://docs.google.com/spreadsheet/ccc?key=0AoDXwrYLJ2lvdFAtRXBPcUs2UjMtY08tZ3NYTEp5dVE&usp=sharing
 */
public class JdbcTypeMapper {

	private static JdbcTypeMapper INSTANCE;
	private final ImmutableMap<Integer, TermType> sqlToTermTypeMap;
	private final ImmutableMap<TermType, Integer> datatypeMap;

	@Inject
	private JdbcTypeMapper() {
		ImmutableMap.Builder<Integer, TermType> sqlToTermBuilder = ImmutableMap.builder();
		sqlToTermBuilder.put(Types.VARCHAR, TYPE_FACTORY.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.CHAR, TYPE_FACTORY.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.LONGNVARCHAR, TYPE_FACTORY.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.LONGVARCHAR, TYPE_FACTORY.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.NVARCHAR, TYPE_FACTORY.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.NCHAR, TYPE_FACTORY.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.INTEGER, TYPE_FACTORY.getXsdIntegerDatatype());
		sqlToTermBuilder.put(Types.BIGINT, TYPE_FACTORY.getXsdIntegerDatatype());
		sqlToTermBuilder.put(Types.SMALLINT, TYPE_FACTORY.getXsdIntegerDatatype());
		sqlToTermBuilder.put(Types.TINYINT, TYPE_FACTORY.getXsdIntegerDatatype());
		sqlToTermBuilder.put(Types.NUMERIC, TYPE_FACTORY.getXsdDecimalDatatype());
		sqlToTermBuilder.put(Types.DECIMAL, TYPE_FACTORY.getXsdDecimalDatatype());
		sqlToTermBuilder.put(Types.FLOAT, TYPE_FACTORY.getXsdDoubleDatatype()); // conversion to float to follow r2rml Natural Mapping of SQL Values
		sqlToTermBuilder.put(Types.DOUBLE, TYPE_FACTORY.getXsdDoubleDatatype());
		sqlToTermBuilder.put(Types.REAL, TYPE_FACTORY.getXsdDoubleDatatype());
		sqlToTermBuilder.put(Types.DATE, TYPE_FACTORY.getDatatype(XSD.DATE));
		sqlToTermBuilder.put(Types.TIME, TYPE_FACTORY.getDatatype(XSD.TIME));
		sqlToTermBuilder.put(Types.TIMESTAMP, TYPE_FACTORY.getDatatype(XSD.DATETIME)); //GX: needs check
		sqlToTermBuilder.put(Types.TIMESTAMP_WITH_TIMEZONE, TYPE_FACTORY.getDatatype(XSD.DATETIME)); //BC: needs check
		sqlToTermBuilder.put(Types.BOOLEAN, TYPE_FACTORY.getXsdBooleanDatatype());
		sqlToTermBuilder.put(Types.BIT, TYPE_FACTORY.getXsdBooleanDatatype());
//		sqlToTermBuilder.put(Types.BINARY, dfac.getDataTypePredicateBinary());
//		sqlToTermBuilder.put(Types.VARBINARY, dfac.getDataTypePredicateBinary());
//		sqlToTermBuilder.put(Types.BLOB, dfac.getDataTypePredicateBinary());
		sqlToTermBuilder.put(Types.CLOB, TYPE_FACTORY.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.OTHER, TYPE_FACTORY.getXsdStringDatatype());
		sqlToTermTypeMap = sqlToTermBuilder.build();

		ImmutableMap.Builder<TermType, Integer> datatypeBuilder = ImmutableMap.builder();
		datatypeBuilder.put(TYPE_FACTORY.getXsdBooleanDatatype(), Types.BOOLEAN);
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.INT), Types.INTEGER);
		datatypeBuilder.put(TYPE_FACTORY.getXsdIntegerDatatype(), Types.BIGINT);
		datatypeBuilder.put(TYPE_FACTORY.getXsdDecimalDatatype(), Types.DECIMAL); //BC: needs to be checked
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.LONG), Types.BIGINT);
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.NEGATIVE_INTEGER), Types.BIGINT);
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.POSITIVE_INTEGER), Types.BIGINT);
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.NON_POSITIVE_INTEGER), Types.BIGINT);
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.UNSIGNED_INT), Types.INTEGER);
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.FLOAT), Types.FLOAT);
		datatypeBuilder.put(TYPE_FACTORY.getXsdDoubleDatatype(), Types.DOUBLE);
		datatypeBuilder.put(TYPE_FACTORY.getXsdStringDatatype(), Types.VARCHAR);
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.DATETIMESTAMP), Types.TIMESTAMP);
		datatypeBuilder.put(TYPE_FACTORY.getDatatype(XSD.DATETIME), Types.TIMESTAMP);
		// all other types are mapped to Types.VARCHAR by default
		datatypeMap = datatypeBuilder.build();
	}

	@Deprecated
	public static JdbcTypeMapper getInstance() {
		if (INSTANCE == null)
			INSTANCE = new JdbcTypeMapper();
		return INSTANCE;
	}

	public TermType getTermType(int sqlType) {
		return Optional.ofNullable(sqlToTermTypeMap.get(sqlType))
				// TODO: use another default type
				.orElse(TYPE_FACTORY.getXsdStringDatatype());
	}

	public int getSQLType(TermType type) {
		if (type != null) {
			Integer sqlType = datatypeMap.get(type);
			if (sqlType != null)
				return sqlType;
		}
		// Return varchar for unknown
		return Types.VARCHAR;
	}
	
}
