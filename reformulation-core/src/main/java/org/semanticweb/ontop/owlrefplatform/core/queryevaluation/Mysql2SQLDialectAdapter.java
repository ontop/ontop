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
import java.util.regex.Pattern;

public class Mysql2SQLDialectAdapter extends SQL99DialectAdapter {

	private static Map<Integer, String> SqlDatatypes;
	static {
		SqlDatatypes = new HashMap<Integer, String>();
		SqlDatatypes.put(Types.INTEGER, "SIGNED");
		SqlDatatypes.put(Types.BIGINT, "SIGNED");
		SqlDatatypes.put(Types.DECIMAL, "DECIMAL");
		SqlDatatypes.put(Types.REAL, "DECIMAL");
		SqlDatatypes.put(Types.FLOAT, "DECIMAL");
		SqlDatatypes.put(Types.DOUBLE, "DECIMAL");
//		SqlDatatypes.put(Types.DOUBLE, "DECIMAL"); // it fails aggregate test with double
		SqlDatatypes.put(Types.CHAR, "CHAR");
		SqlDatatypes.put(Types.VARCHAR, "CHAR(8000) CHARACTER SET utf8");  // for korean, chinese, etc characters we need to use utf8
		SqlDatatypes.put(Types.DATE, "DATE");
		SqlDatatypes.put(Types.TIME, "TIME");
		SqlDatatypes.put(Types.TIMESTAMP, "DATETIME");
		SqlDatatypes.put(Types.BOOLEAN, "BOOLEAN");
	}
	
	@Override
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		if (strings.length == 1)
			return strings[0];
		
		StringBuilder sql = new StringBuilder();

		sql.append(String.format("CONCAT(%s", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(", %s", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}
	
	@Override
	public String sqlQualifiedColumn(String tablename, String columnname) {
		return String.format("%s.`%s`", tablename, columnname);
	}

	@Override
	public String sqlTableName(String tablename, String viewname) {
		return String.format("%s %s", tablename, viewname);
	}

	/*Now we use the table name given by the user, 
	  and we assume that it includes the quotes if needed*/
	@Override
	public String sqlQuote(String name) {
		return String.format("`%s`", name);
	}

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 || limit == 0) {
			if (offset < 0) {
				/* If both limit and offset is not specified.
				 */
				return "LIMIT 0";
			} else {
				/* If the limit is not specified then put a big number as suggested 
				 * in http://dev.mysql.com/doc/refman/5.0/en/select.html
				 */
				return String.format("LIMIT %d,18446744073709551615", offset);
			}
		} else {
			if (offset < 0) {
				// If the offset is not specified
				return String.format("LIMIT %d", limit);
			} else {
				return String.format("LIMIT %d,%d", offset, limit);
			}
		}
	}
	
	@Override
	    public String sqlCast(String value, int type) {
		String strType = SqlDatatypes.get(type);
		
		boolean noCast = strType.equals("BOOLEAN");

		if (strType != null && !noCast ) {	
			return "CAST(" + value + " AS " + strType + ")";
		} else	if (noCast){
				return value;
			
		}
		throw new RuntimeException("Unsupported SQL type");
	}
	
	/**
	 * Based on information in MySQL 5. 1 manual at
	 * http://dev.mysql.com/doc/refman/5.1/en/regexp.html
	 * and
	 * http://dev.mysql.com/doc/refman/5.1/en/pattern-matching.html
	 */
	@Override
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {
        Pattern quotes = Pattern.compile("[\"`\\['].*[\"`\\]']");
		if(quotes.matcher(pattern).matches() ) {
		pattern = pattern.substring(1, pattern.length() - 1); // remove the
		// enclosing
		// quotes
        }
		String sql = columnname + " REGEXP ";
		if (!caseinSensitive) 
			sql += "BINARY ";
			
		return sql + "'" + pattern + "'";
	}

	@Override
	public String getDummyTable() {
		return "SELECT 1";
	}
	
	@Override 
	public String getSQLLexicalFormBoolean(boolean value) {
		return value ? 	"TRUE" : "FALSE";
	}

	/***
	 * Given an XSD dateTime this method will generate a SQL TIMESTAMP value.
	 * The method will strip any fractional seconds found in the date time
	 * (since we haven't found a nice way to support them in all databases). It
	 * will also normalize the use of Z to the timezome +00:00 and last, if the
	 * database is H2, it will remove all timezone information, since this is
	 * not supported there.
	 * 
	 *
	 * @return
	 */
	@Override
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
		bf.insert(0, "'");
		bf.append("'");
		
		return bf.toString();
	}
	
}
