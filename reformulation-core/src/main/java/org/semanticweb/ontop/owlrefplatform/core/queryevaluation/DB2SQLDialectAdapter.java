package org.semanticweb.ontop.owlrefplatform.core.queryevaluation;

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

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class DB2SQLDialectAdapter extends SQL99DialectAdapter {
	private static Map<Integer, String> SqlDatatypes;
	static {
		SqlDatatypes = new HashMap<Integer, String>();
		SqlDatatypes.put(Types.INTEGER, "INTEGER");
		SqlDatatypes.put(Types.DECIMAL, "DECIMAL");
		SqlDatatypes.put(Types.REAL, "REAL");
		SqlDatatypes.put(Types.FLOAT, "DOUBLE");
		SqlDatatypes.put(Types.DOUBLE, "DOUBLE");
//		SqlDatatypes.put(Types.DOUBLE, "DECIMAL"); // it fails aggregate test with double
		SqlDatatypes.put(Types.CHAR, "CHAR");
		SqlDatatypes.put(Types.VARCHAR, "VARCHAR(100)");  // for korean, chinese, etc characters we need to use utf8
		SqlDatatypes.put(Types.DATE, "TIMESTAMP");
		SqlDatatypes.put(Types.TIME, "TIME");
		SqlDatatypes.put(Types.TIMESTAMP, "TIMESTAMP");
		SqlDatatypes.put(Types.BOOLEAN, "BOOLEAN");
		SqlDatatypes.put(Types.BIGINT, "BIGINT");
	}
	@Override
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		if (strings.length == 1)
			return strings[0];
		StringBuilder sql = new StringBuilder();

		sql.append(String.format("(%s", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(" || %s", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 || limit == 0) {
			if (offset < 0) {
				// If both limit and offset is not specified.
				return "";
			} else {
				// The max number of rows is specified by the development team.
				return String.format("LIMIT 8000\nOFFSET %d", offset);
			}
		} else {
			if (offset < 0) {
				// If the offset is not specified
				return String.format("LIMIT %d\n", limit);
			} else {
				return String.format("LIMIT %d\nOFFSET %d", limit, offset);
			}
		}
	}

	@Override
	public String sqlCast(String value, int type) {
		String strType = SqlDatatypes.get(type);

		if (strType == null) {
			throw new RuntimeException("Unsupported SQL type (not recognized)");
		}

		boolean noCast = strType.equals("BOOLEAN");

		if (strType != null && !noCast ) {	
			return "CAST(" + value + " AS " + strType + ")";
		} else	if (noCast){
				return value;
			
		}
		throw new RuntimeException("Unsupported SQL type");
	}
	
	@Override
	public String getDummyTable() {
		return "SELECT 1 from sysibm.sysdummy1";
	}

	@Override 
	public String getSQLLexicalFormBoolean(boolean value) {
		return value ? 	"1" : "0";
	}

	/***
	 * Given an XSD dateTime this method will generate a SQL TIMESTAMP value.
	 * The method will strip any fractional seconds found in the date time
	 * (since we haven't found a nice way to support them in all databases). It
	 * will also normalize the use of Z to the timezome +00:00 and last, if the
	 * database is H2, it will remove all timezone information, since this is
	 * not supported there.
	 * 
	 * @param rdfliteral
	 * @return
	 */
	public String getSQLLexicalFormDatetime(String v) {
		String datetime = v.replace('T', ' ');
		int dotlocation = datetime.indexOf('.');
		int zlocation = datetime.indexOf('Z');
		int minuslocation = datetime.indexOf('-', 10); // added search from 10th pos, because we need to ignore minuses in date
		int pluslocation = datetime.indexOf('+');
		StringBuilder bf = new StringBuilder(datetime);
		if (zlocation != -1) {
			/*
			 * replacing Z by +00:00
			 */
			bf.replace(zlocation, bf.length(), "+00:00");
		}

		if (dotlocation != -1) {
			/*
			 * Stripping the string from the presicion that is not supported by
			 * SQL timestamps.
			 */
			// TODO we need to check which databases support fractional
			// sections (e.g., oracle,db2, postgres)
			// so that when supported, we use it.
			int endlocation = Math.max(zlocation, Math.max(minuslocation, pluslocation));
			if (endlocation == -1) {
				endlocation = datetime.length();
			}
			bf.replace(dotlocation, endlocation, "");
		}
		if (bf.length() > 19) {
			bf.delete(19, bf.length());
		}
		bf.insert(0, "'");
		bf.append("'");
		
		return bf.toString();
	}

	
}
