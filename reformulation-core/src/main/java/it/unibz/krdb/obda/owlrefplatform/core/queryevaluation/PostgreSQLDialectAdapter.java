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
import java.util.regex.Pattern;

public class PostgreSQLDialectAdapter extends SQL99DialectAdapter {

    private Pattern quotes = Pattern.compile("[\"`\\['].*[\"`\\]']");

    @Override
    public String MD5(String str){
    	return String.format("MD5(%s)", str);
    }
    
    @Override
	public String strSubstr(String str, String start, String end) {
		return String.format("SUBSTRING(%s FROM %s FOR %s)", str, start, end);
	} 
    
    @Override
	public String strBefore(String str, String before) {
		return String.format("LEFT(%s,CAST (SIGN(POSITION(%s IN %s))*(POSITION(%s IN %s)-1) AS INTEGER))", str, before, str, before, str);
		}
    
    @Override
	public String strStartsOperator(){
		return "LEFT(%1$s, LENGTH(%2$s)) LIKE %2$s";	
	} 
    
    @Override
	public String strContainsOperator(){
		return "POSITION(%2$s IN %1$s) > 0";		
	}
    
    @Override
	public String strAfter(String str, String after) {
//SIGN return a double precision, we need to cast to numeric to avoid conversion exception while using substring
		return String.format("SUBSTRING(%s,POSITION(%s IN %s) + LENGTH(%s), CAST( SIGN(POSITION(%s IN %s)) * LENGTH(%s) AS INTEGER))",
				str, after, str , after , after, str, str);

	}

    @Override
    public String dateTZ(String str){
    	return strConcat(new String[]{String.format("EXTRACT(TIMEZONE_HOUR FROM %s)", str), "':'",String.format("EXTRACT(TIMEZONE_MINUTE FROM %s)", str) });
    }

	@Override
	public String dateNow(){
		return "NOW()";
	}
    
	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 ) {
			if (offset < 0) {
				// If both limit and offset is not specified.
				return "";
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

	@Override //trick to support uuid
	public String strUuid() {
		return "md5(random()::text || clock_timestamp()::text)::uuid";
	}

	@Override
	public String uuid() {
		return "'urn:uuid:'|| md5(random()::text || clock_timestamp()::text)::uuid";
	}

	@Override
	public String rand() {
		return "RANDOM()";
	}

	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR(10485760)";
		} else {
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

        if(quotes.matcher(pattern).matches() ) {
            pattern = pattern.substring(1, pattern.length() - 1); // remove the
            // enclosing
            // quotes
        }
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

    @Override
    public String strReplace(String str, String oldstr, String newstr) {

        if(quotes.matcher(oldstr).matches() ) {
            oldstr = oldstr.substring(1, oldstr.length() - 1); // remove the enclosing quotes
        }

        if(quotes.matcher(newstr).matches() ) {
            newstr = newstr.substring(1, newstr.length() - 1);
        }
        return String.format("REGEXP_REPLACE(%s, '%s', '%s')", str, oldstr, newstr);
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
	 * @param v
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
