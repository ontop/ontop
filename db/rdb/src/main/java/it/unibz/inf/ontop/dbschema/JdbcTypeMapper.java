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
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;

import java.sql.Types;
import java.util.Optional;

/**
 * This class maps SQL datatypes to XML datatypes.
 * Details look at this link: https://docs.google.com/spreadsheet/ccc?key=0AoDXwrYLJ2lvdFAtRXBPcUs2UjMtY08tZ3NYTEp5dVE&usp=sharing
 */
public class JdbcTypeMapper implements TypeMapper {

	private final ImmutableMap<Integer, TermType> sqlToTermTypeMap;
	private final ImmutableMap<TermType, Integer> datatypeMap;
	private final TypeFactory typeFactory;

	@Inject
	private JdbcTypeMapper(TypeFactory typeFactory) {
		this.typeFactory = typeFactory;
		ImmutableMap.Builder<Integer, TermType> sqlToTermBuilder = ImmutableMap.builder();
		sqlToTermBuilder.put(Types.VARCHAR, typeFactory.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.CHAR, typeFactory.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.LONGNVARCHAR, typeFactory.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.LONGVARCHAR, typeFactory.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.NVARCHAR, typeFactory.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.NCHAR, typeFactory.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.INTEGER, typeFactory.getXsdIntegerDatatype());
		sqlToTermBuilder.put(Types.BIGINT, typeFactory.getXsdIntegerDatatype());
		sqlToTermBuilder.put(Types.SMALLINT, typeFactory.getXsdIntegerDatatype());
		sqlToTermBuilder.put(Types.TINYINT, typeFactory.getXsdIntegerDatatype());
		sqlToTermBuilder.put(Types.NUMERIC, typeFactory.getXsdDecimalDatatype());
		sqlToTermBuilder.put(Types.DECIMAL, typeFactory.getXsdDecimalDatatype());
		sqlToTermBuilder.put(Types.FLOAT, typeFactory.getXsdDoubleDatatype()); // conversion to float to follow r2rml Natural Mapping of SQL Values
		sqlToTermBuilder.put(Types.DOUBLE, typeFactory.getXsdDoubleDatatype());
		sqlToTermBuilder.put(Types.REAL, typeFactory.getXsdDoubleDatatype());
		sqlToTermBuilder.put(Types.DATE, typeFactory.getDatatype(XSD.DATE));
		sqlToTermBuilder.put(Types.TIME, typeFactory.getDatatype(XSD.TIME));
		sqlToTermBuilder.put(Types.TIMESTAMP, typeFactory.getDatatype(XSD.DATETIME)); //GX: needs check
		sqlToTermBuilder.put(Types.TIMESTAMP_WITH_TIMEZONE, typeFactory.getDatatype(XSD.DATETIME)); //BC: needs check
		sqlToTermBuilder.put(Types.BOOLEAN, typeFactory.getXsdBooleanDatatype());
		sqlToTermBuilder.put(Types.BIT, typeFactory.getXsdBooleanDatatype());
//		sqlToTermBuilder.put(Types.BINARY, dfac.getDataTypePredicateBinary());
//		sqlToTermBuilder.put(Types.VARBINARY, dfac.getDataTypePredicateBinary());
//		sqlToTermBuilder.put(Types.BLOB, dfac.getDataTypePredicateBinary());
		sqlToTermBuilder.put(Types.CLOB, typeFactory.getXsdStringDatatype());
		sqlToTermBuilder.put(Types.OTHER, typeFactory.getXsdStringDatatype());
		sqlToTermTypeMap = sqlToTermBuilder.build();

		ImmutableMap.Builder<TermType, Integer> datatypeBuilder = ImmutableMap.builder();
		datatypeBuilder.put(typeFactory.getXsdBooleanDatatype(), Types.BOOLEAN);
		datatypeBuilder.put(typeFactory.getDatatype(XSD.INT), Types.INTEGER);
		datatypeBuilder.put(typeFactory.getXsdIntegerDatatype(), Types.BIGINT);
		datatypeBuilder.put(typeFactory.getXsdDecimalDatatype(), Types.DECIMAL); //BC: needs to be checked
		datatypeBuilder.put(typeFactory.getDatatype(XSD.LONG), Types.BIGINT);
		datatypeBuilder.put(typeFactory.getDatatype(XSD.NEGATIVE_INTEGER), Types.BIGINT);
		datatypeBuilder.put(typeFactory.getDatatype(XSD.POSITIVE_INTEGER), Types.BIGINT);
		datatypeBuilder.put(typeFactory.getDatatype(XSD.NON_POSITIVE_INTEGER), Types.BIGINT);
		datatypeBuilder.put(typeFactory.getDatatype(XSD.UNSIGNED_INT), Types.INTEGER);
		datatypeBuilder.put(typeFactory.getDatatype(XSD.FLOAT), Types.FLOAT);
		datatypeBuilder.put(typeFactory.getXsdDoubleDatatype(), Types.DOUBLE);
		datatypeBuilder.put(typeFactory.getXsdStringDatatype(), Types.VARCHAR);
		datatypeBuilder.put(typeFactory.getDatatype(XSD.DATETIMESTAMP), Types.TIMESTAMP);
		datatypeBuilder.put(typeFactory.getDatatype(XSD.DATETIME), Types.TIMESTAMP);
		// all other types are mapped to Types.VARCHAR by default
		datatypeMap = datatypeBuilder.build();
	}

	@Override
	public TermType getTermType(int sqlType, String typeName) {
		return Optional.ofNullable(sqlToTermTypeMap.get(sqlType))
				// TODO: use another default type
				.orElse(typeFactory.getXsdStringDatatype());
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
