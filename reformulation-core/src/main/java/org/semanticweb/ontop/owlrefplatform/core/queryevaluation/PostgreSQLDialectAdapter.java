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

public class PostgreSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 || limit == 0) {
			if (offset < 0) {
				// If both limit and offset is not specified.
				return "LIMIT 0";
			} else {
				// if the limit is not specified
				return String.format("LIMIT ALL\nOFFSET %d", offset);
			}
		} else {
			if (offset < 0) {
				// If the offset is not specified
				return String.format("LIMIT %d\nOFFSET 0", limit);
			} else {
				return String.format("LIMIT %d\nOFFSET %d", limit, offset);
			}
		}
	}
	
	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		
		switch (type) {
		case Types.VARCHAR:
			strType = "CHAR";
			break;
		case Types.BIT:
			strType = "BIT";			break;
		case Types.TINYINT:
			strType = "TINYINT";			break;
		case Types.SMALLINT:
			strType = "SMALLINT";			break;
		case Types.INTEGER:
			strType = "INTEGER";			break;
		case Types.BIGINT:
			strType = "BIGINT";			break;
		case Types.FLOAT:
			strType = "FLOAT";			break;
		case Types.REAL:
			strType = "REAL";			break;
		case Types.DOUBLE:
			strType = "double precision";			break;
		case Types.NUMERIC:
			strType = "NUMERIC";			break;
		case Types.DECIMAL:
			strType = "DECIMAL";			break;
		case Types.CHAR:
			strType = "CHAR";			break;
		case Types.LONGVARCHAR:
			strType = "LONGVARCHAR";			break;
		case Types.DATE:
			strType = "DATE";			break;
		case Types.TIME:
			strType = "TIME";			break;
		case Types.TIMESTAMP:
			strType = "TIMESTAMP";			break;
		case Types.BINARY:
			strType = "BINARY";			break;
		case Types.VARBINARY:
			strType = "VARBINARY";			break;
		case Types.LONGVARBINARY:
			strType = "LONGVARBINARY";			break;
		case Types.NULL:
			strType = "NULL";			break;
		case Types.OTHER:
			strType = "OTHER";			break;
		case Types.JAVA_OBJECT:
			strType = "JAVA_OBJECT";			break;
		case Types.DISTINCT:
			strType = "DISTINCT";			break;
		case Types.STRUCT:
			strType = "STRUCT";			break;
		case Types.ARRAY:
			strType = "ARRAY";			break;
		case Types.BLOB:
			strType = "BLOB";			break;
		case Types.CLOB:
			strType = "CLOB";			break;
		case Types.REF:
			strType = "REF";			break;
		case Types.DATALINK:
			strType = "DATALINK";			break;
		case Types.BOOLEAN:
			strType = "BOOLEAN";			break;
		case Types.ROWID:
			strType = "ROWID";			break;
		case Types.NCHAR:
			strType = "NCHAR";			break;
		case Types.NVARCHAR:
			strType = "NVARCHAR";			break;
		case Types.LONGNVARCHAR:
			strType = "LONGNVARCHAR";			break;
		case Types.NCLOB:
			strType = "NCLOB";			break;
		case Types.SQLXML:
			strType = "SQLXML";			break;


		default:
			throw new RuntimeException("Unsupported SQL type");

		}
		return "CAST(" + value + " AS " + strType + ")";
	}
	
	/**
	 * Based on documentation of postgres 9.1 at 
	 * http://www.postgresql.org/docs/9.3/static/functions-matching.html
	 */
	@Override
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {
		pattern = pattern.substring(1, pattern.length() - 1); // remove the
																// enclosing
																// quotes
		//An ARE can begin with embedded options: a sequence (?n)  specifies options affecting the rest of the RE. 
		//n is newline-sensitive matching
		String flags = "";
		if (multiLine)
			flags = "(?w)"; //inverse partial newline-sensitive matching
		else
		if(dotAllMode)
			flags = "(?p)"; //partial newline-sensitive matching
		
		return columnname + " ~" + ((caseinSensitive)? "* " : " ") + "'"+ ((multiLine && dotAllMode)? "(?n)" : flags) + pattern + "'";
	}
}
