package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

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

public class TeiidSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String dateNow(){
		return "NOW()";
	}

	@Override
	public String round() {
		return "ROUND(%s, 0)";
	}

	@Override
	public String ceil() {
		return "CEILING(%s)";
	}

	@Override
	public String strUcase(String str) {
		return String.format("UCASE(%s)", str);
	}

	@Override
	public String strStartsOperator(){
		return "SUBSTRING(%1$s, 1, CHAR_LENGTH(%2$s)) LIKE %2$s";
	}

	@Override
	public String strEndsOperator(){
		return "RIGHT(%1$s, CHAR_LENGTH(%2$s)) LIKE %2$s";
	}

	@Override
	public String strContainsOperator(){
		return "LOCATE(%2$s,%1$s) > 0";
	}

	@Override
	public String strBefore(String str, String before) {
		return String.format("LEFT(%s,LOCATE(%s,%s)-1)", str,  before, str);
	}

	@Override
	public String strAfter(String str, String after) {
//		sign return 1 if positive number, 0 if 0 and -1 if negative number
//		it will return everything after the value if it is present or it will return an empty string if it is not present
		return String.format("SUBSTRING(%s,LOCATE(%s,%s) + LENGTH(%s), SIGN(LOCATE(%s,%s)) * LENGTH(%s))",
				str, after, str , after , after, str, str);
	}

	@Override
	public String strLcase(String str) {
		return String.format("LCASE(%s)", str);
	}

	@Override
	public String strUuid(){
		return "UUID()";
	}

	@Override
	public String uuid(){
		return strConcat(new String[]{"'urn:uuid:'", "UUID()"});
	}

	@Override
	public String dateTZ(String str) {
		return strConcat(new String[] {String.format("EXTRACT(TIMEZONE_HOUR FROM %s)", str), "':'" , String.format("EXTRACT(TIMEZONE_MINUTE FROM %s) ",str)});
	}


	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
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
		// TODO: check whether this inherited implementation is OK
		
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

	/**
	 * See https://docs.jboss.org/teiid/7.7.0.Final/reference/en-US/html/sql_clauses.html#limit_clause
	 */
	@Override
	public String sqlSlice(long limit, long offset) {
		if ((limit < 0) && (offset < 0)) {
			return "";
		}
		else if ((limit >= 0) && (offset >= 0)) {
			return String.format("LIMIT %d, %d", offset, limit);
		}
		else if (offset < 0) {
			return String.format("LIMIT %d", limit);
		}
		// Else -> (limit < 0)
		else {
			return String.format("OFFSET %d ROWS", offset);
		}
	}
	
}
