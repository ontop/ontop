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

import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Set;

import org.openrdf.model.util.URIUtil;

public class SQL99DialectAdapter implements SQLDialectAdapter {

    private Pattern quotes = Pattern.compile("[\"`\\['].*[\"`\\]']");  
    
  
    
    @Override 
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
  	public String dateNow() {
    	return "CURRENT_TIMESTAMP()";
    	
  	}
    
    @Override
  	public String dateYear(String str) {
    	return String.format("EXTRACT(YEAR FROM %s)",str);
  	}
    
    @Override
  	public String dateDay(String str) {
    	return String.format("EXTRACT(DAY FROM %s)",str);
  	}
    
    @Override
  	public String dateHours(String str) {
    	return String.format("EXTRACT(HOUR FROM %s)",str);
  	}
    
    @Override
  	public String dateMonth(String str) {
    	return String.format("EXTRACT(MONTH FROM %s)",str);
  	}
    
    @Override
  	public String dateMinutes(String str) {
    	return String.format("EXTRACT(MINUTE FROM %s)",str);
  	}
    
    @Override
  	public String dateSeconds(String str) {
    	return String.format("EXTRACT(SECOND FROM %s)",str);
  	}
    
    @Override
  	public String dateTimezone(String str) {
    	throw new UnsupportedOperationException("TIMEZONE is not supported in this dialect.");
  	}
    
  
    @Override
  	public String SHA256(String str) {
    	throw new UnsupportedOperationException("SHA256 is not supported in this dialect.");
  	}
    
    @Override
  	public String SHA1(String str) {
    	throw new UnsupportedOperationException("SHA1 is not supported in this dialect.");
  	}
    
    @Override
  	public String SHA512(String str) {
    	throw new UnsupportedOperationException("SHA512 is not supported in this dialect.");
  	}
      
      @Override
  	public String MD5(String str) {
      	throw new UnsupportedOperationException("MD5 is not supported in this dialect.");
  	}
    
    @Override
	public String uuid() {
		return "UUID()";
	}
    
	@Override
	public String strConcat(String[] strings) {
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
	public String strUcase(String str) {
		return String.format("UPPER(%s)", str);
	} 
	
	@Override
	public String strStartsOperator(){
		return "SUBSTRING(%1$s, 0, LENGTH(%2$s)) LIKE %2$s";	
	}
	
	@Override
	public String strEndsOperator(){
		return "RIGHT(%1$s, LENGTH(%2$s)) LIKE %2$s";
	}
	
	@Override
	public String strContainsOperator(){
		return "CHARINDEX(%2$s,%1$s) > 0";		
	}
	
	@Override
	public String strBefore(String str, String before) {
		return String.format("LEFT(%s,CHARINDEX(%s,%s)-1)", str, before, str);
	} 
	
	@Override
	public String strAfter(String str, String after) {
		return String.format("SUBSTR(%s,CHARINDEX(REGEXP_REPLACE(%s, (CONCAT('.*',%s) ), ''),%s))",
				str, str, after,str); //FIXME when no match found should return empty string
	}
	
	@Override
	public String strLcase(String str) {
		return String.format("LOWER(%s)", str);
	}
	
	@Override
	public String strLength(String str) {
		return String.format("LENGTH(%s)", str);
	} 
	
	@Override
	public String strSubstr(String str, String start, String end) {
		return String.format("SUBSTR(%s,%s,%s)", str, start, end);
	} //Nika
	
	@Override
	public String strReplace(String str, char oldchar, char newchar) {
		return String.format("REPLACE(%s, '%s', '%s')", str, oldchar, newchar);
	}

	@Override
	public String strReplace(String str, String oldstr, String newstr) {
        if(quotes.matcher(oldstr).matches() ) {
            oldstr = oldstr.substring(1, oldstr.length() - 1); // remove the enclosing quotes
        }

        if(quotes.matcher(newstr).matches() ) {
            newstr = newstr.substring(1, newstr.length() - 1);
        }
		return String.format("REPLACE(%s, '%s', '%s')", str, oldstr, newstr);
	}

	@Override
	public String strReplace(String str, int start, int end, String with) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strIndexOf(String str, char ch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strIndexOf(String str, String strsr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sqlQualifiedColumn(String tablename, String columnname) {
		// TODO: This should depend on whether the column name was quoted in the original sql query
		return String.format("%s.\"%s\"", tablename, columnname);
	}

	
	@Override
//	public String sqlTableName(String tablename, String viewname) {
//		return String.format("\"%s\" %s", tablename, viewname);
//	}
	
	/*Now we use the table name given by the user, 
	  and we assume that it includes the quotes if needed*/
	public String sqlTableName(String tablename, String viewname) {
		return String.format("%s %s", tablename, viewname);
	}

	@Override
	public String sqlQuote(String name) {
		//TODO: This should depend on quotes in the sql in the mappings
		return String.format("\"%s\"", name);
//		return name;
	}

	/**
	 * There is no standard for this part.
	 *
	 * Arbitrary default implementation proposed
	 * (may not work with many DB engines).
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
			return String.format("OFFSET %d", offset);
		}
	}

	@Override
	public String sqlOrderBy(List<OrderCondition> conditions, String viewname) {
		String sql = "ORDER BY ";
		boolean needComma = false;
		for (OrderCondition c : conditions) {
			if (needComma) {
				sql += ", ";
			}
			sql += sqlQualifiedColumn(viewname, c.getVariable().getName());
			if (c.getDirection() == OrderCondition.ORDER_DESCENDING) {
				sql += " DESC";
			}
			needComma = true;
		}
		return sql;
	}

	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "CHAR";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
	}

	@Override
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {

        if(quotes.matcher(pattern).matches() ) {
            pattern = pattern.substring(1, pattern.length() - 1); // remove the
            // enclosing
            // quotes
        }
		//we use % wildcards to search for a string that contains and not only match the pattern
		if (caseinSensitive) {
			return " LOWER(" + columnname + ") LIKE " + "'%"
					+ pattern.toLowerCase() + "%'";
		}
		return columnname + " LIKE " + "'%" + pattern + "%'";
	}
	
	
	@Override
	public String getDummyTable() {
		// TODO: check whether this inherited implementation from JDBCUtilities is OK
		return "SELECT 1";
	}

	@Override
	public String getSQLLexicalFormString(String constant) {
		return "'" + constant + "'";
	}
	
	@Override 
	public String getSQLLexicalFormBoolean(boolean value) {
		// TODO: check whether this implementation inherited from JDBCUtility is correct
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
	 */
	@Override
	public String getSQLLexicalFormDatetime(String v) {
		// TODO: check whether this implementation inherited from JDBCUtility is correct
		
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

	@Override
	public String getSQLLexicalFormDatetimeStamp(String v) {
		// TODO: check whether this implementation inherited from JDBCUtility is correct

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

	@Override
	public String nameTopVariable(String signatureVariableName, String proposedSuffix, Set<String> sqlVariableNames) {
		return sqlQuote(signatureVariableName + proposedSuffix);
	}

	




}
