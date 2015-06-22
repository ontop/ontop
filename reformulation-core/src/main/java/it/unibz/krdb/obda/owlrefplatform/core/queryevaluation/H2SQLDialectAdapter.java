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

import java.util.regex.Pattern;

public class H2SQLDialectAdapter extends SQL99DialectAdapter {
	
	@Override
	public String uuid(){
		return "RANDOM_UUID()";
	}
	
	public String strEncodeForUri(String str){
	      return "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" +
	            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" + str + ",' ', '%20')," +
	            "'!', '%21')," +
	            "'@', '%40')," +
	            "'#', '%23')," +
	            "'$', '%24')," +
	            "'&', '%26')," +
	            "'*', '%42'), " +
	            "'(', '%28'), " +
	            "')', '%29'), " +
	            "'[', '%5B'), " +
	            "']', '%5D'), " +
	            "',', '%2C'), " +
	            "';', '%3B'), " +
	            "':', '%3A'), " +
	            "'?', '%3F'), " +
	            "'=', '%3D'), " +
	            "'+', '%2B'), " +
	            "'''', '%22'), " +
	            "'/', '%2F')";
	    }
	
	@Override
	public String SHA256(String str){
		return String.format("HASH('SHA256', STRINGTOUTF8(%s),1)", str);
	}


	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 || limit == 0) {
			if (offset < 0) {
				// If both limit and offset is not specified.
				return "LIMIT 0";
			} else {
				// Max limit: http://www.h2database.com/html/advanced.html#limits_limitations
				return String.format("LIMIT %d,2147483647", offset);
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
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {
        Pattern quotes = Pattern.compile("[\"`\\['].*[\"`\\]']");
        if(quotes.matcher(pattern).matches() ) {
            pattern = pattern.substring(1, pattern.length() - 1); // remove the
            // enclosing
            // quotes
        }
		// embedded options: 
		
		String pflags = "(?";
		if (multiLine)
			pflags += "m"; // equivalent of Pattern.MULTILINE
		if (dotAllMode)
			pflags += "s"; // equivalent of Pattern.DOTALL
		pflags +=")";
		return columnname + " ~" + ((caseinSensitive) ? "* " : " ") + "'"+ ((multiLine || dotAllMode) ? pflags : "") + pattern + "'";
	}

    @Override
    public String strReplace(String str, String oldstr, String newstr) {
        oldstr = oldstr.substring(1, oldstr.length() - 1); // remove the enclosing quotes

        newstr = newstr.substring(1, newstr.length() - 1);
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
	 * @param
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
