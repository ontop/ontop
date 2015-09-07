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

public class SQLServerSQLDialectAdapter extends SQL99DialectAdapter {
	
	 @Override
	  	public String SHA256(String str) {
	    	return String.format("LOWER(CONVERT(VARCHAR(64),  HashBytes('SHA2_256',%s),2 ))", str);
	  	}
	    
	    @Override
	  	public String SHA1(String str) {
	    	return String.format("LOWER(CONVERT(VARCHAR(40), HASHBYTES('SHA1',%s),2 ))", str);
	  	}
	    
	    @Override
	  	public String SHA512(String str) {
	    	return String.format("LOWER(CONVERT(VARCHAR(128),HASHBYTES('SHA2_512',%s) ,2 ))", str);
	  	}
	      
	      @Override
	  	public String MD5(String str) {
		    	return String.format("LOWER(CONVERT(VARCHAR(40), HASHBYTES('MD5',%s) ,2 ))", str);
	  	}

	@Override
	public String dateYear(String str) {
		return String.format("YEAR ( %s)",str);
	}

	@Override
	public String dateDay(String str) {
		return String.format("DAY ( %s)",str);
	}

	@Override
	public String dateHours(String str) {
		return String.format("HOUR ( %s)",str);
	}

	@Override
	public String dateMonth(String str) {
		return String.format("MONTH (%s)",str);
	}

	@Override
	public String dateMinutes(String str) {
		return String.format("MINUTE (%s)",str);
	}

	@Override
	public String dateSeconds(String str) {
		return String.format("SECOND (%s)",str);
	}

	@Override
	public String ceil() {
		return "CEILING(%s)";
	}

	@Override
	public String round() {
		return "ROUND(%s, 0)";
	}

	@Override
	public String strStartsOperator(){
		return "LEFT(%1$s, LEN(%2$s)) LIKE %2$s";
	}

	@Override
	public String strEndsOperator(){
		return "RIGHT(%1$s, LEN(%2$s)) LIKE %2$s";
	}

	@Override
	public String strContainsOperator(){
		return "CHARINDEX(%2$s,%1$s) > 0";
	}

	@Override
	public String strBefore(String str, String before) {
		return String.format("LEFT(%s,SIGN(CHARINDEX(%s,%s))* (CHARINDEX(%s,%s)-1))", str, before, str, before, str);

	}

	@Override
	public String strAfter(String str, String after) {
		return String.format("SUBSTRING(%s,CHARINDEX(%s,%s)+LEN(%s),SIGN(CHARINDEX(%s,%s))*LEN(%s))",
				str, after, str , after , after, str, str); //FIXME when no match found should return empty string
	}

	@Override
	public String strSubstr(String str, String start, String end) {
		return String.format("SUBSTRING(%s,%s,%s)", str, start, end);
	}

	@Override
	public String strLength(String str) {
		return String.format("LEN(%s)", str);
	}

	@Override
	public String strConcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		
		if (strings.length == 1)
			return strings[0];
		
		StringBuilder sql = new StringBuilder();

		sql.append(String.format("(CAST (%s as varchar(8000))", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(" + CAST(%s as varchar(8000))", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}
	
	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 || limit == 0) {
			if (offset < 0) {
				return "OFFSET 0 ROWS";
			} else {
				return String.format("OFFSET %d ROWS", offset);
			}
		} else {
			if (offset < 0) {
				// If the offset is not specified
				return String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit);
			} else {
				return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
			}
		}
	}
	
	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR(8000)";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
	}
	
	public String sqlLimit(String originalString, long limit) {
		final String limitStmt = String.format("TOP %d ", limit);
		StringBuilder sb = new StringBuilder(originalString);
		int insertPosition = originalString.indexOf(" ") + 1;
		sb.insert(insertPosition, limitStmt);
		return sb.toString();
	}

	@Override
	public String getDummyTable() {
		return "SELECT 1 as \"example\"";
	}
	
	@Override 
	public String getSQLLexicalFormBoolean(boolean value) {
		return value ? 	"'TRUE'" : "'FALSE'";
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
		if (bf.length() > 19) {
			bf.delete(19, bf.length());
		}
		bf.insert(0, "'");
		bf.append("'");
		
		return bf.toString();
	}


}
