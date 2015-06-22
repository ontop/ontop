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

import java.util.List;
import java.util.Set;

public interface SQLDialectAdapter {
	
	public String MD5(String literal);
	
	public String SHA256(String str);
	
	public String SHA512(String str);
	
	public String SHA1(String str);

	public String uuid();
	
	public String strStartsOperator();
	
	public String strEndsOperator();
	
	public String strContainsOperator();
	
	public String strLength(String str); 
	
	public String strUcase(String str);
	
	public String strLcase(String str);
	
	public String strEncodeForUri(String str);

	public String strBefore(String str, String before);
	
	public String strAfter(String str, String after);
	
	public String strSubstr(String str, String start, String end); 

	public String strConcat(String[] strings);

	public String strReplace(String str, char oldchar, char newchar);

	public String strReplace(String str, String oldstr, String newstr);

	public String strReplace(String str, int start, int end, String with);

	public String strIndexOf(String str, char ch);

	public String strIndexOf(String str, String strsr);
	
	/*
	 * Table/Column name functions
	 */

	public String sqlQualifiedColumn(String tablename, String columnname);

	public String sqlTableName(String tablename, String viewname);

	public String sqlQuote(String name);

	public String sqlSlice(long limit, long offset);

	public String sqlOrderBy(List<OrderCondition> conditions, String viewname);

	/***
	 * Returns the expression that cast's the given column into a particular SQL
	 * type. The types are those enumerated in java.sql.Types.
	 * 
	 * @param value
	 * @param type
	 *            A static constant from java.sql.Types.
	 * @return
	 */
	public String sqlCast(String value, int type);
	
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode);
	
	public String getDummyTable();
	
	public String getSQLLexicalFormString(String constant);
	
	public String getSQLLexicalFormBoolean(boolean value);
	
	public String getSQLLexicalFormDatetime(String value);

	public String getSQLLexicalFormDatetimeStamp(String v);


	/**
	 * Allows the SQL dialect adapter to put restrict on the name (e.g. name length).
	 */
	public String nameTopVariable(String signatureVariable, String proposedSuffix, Set<String> sqlVariableNames);

	public String dateNow();
	
	public String dateYear(String date);

	public String dateHours(String date);

	public String dateMinutes(String date);

	public String dateSeconds(String date);

	public String dateDay(String date);

	public String dateTimezone(String date);

	public String dateMonth(String date);
}
