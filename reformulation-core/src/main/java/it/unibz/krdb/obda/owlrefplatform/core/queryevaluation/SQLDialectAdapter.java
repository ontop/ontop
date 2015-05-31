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
	
	public String strlength(String str); //added by Nika

	public String strconcat(String[] strings);

	public String strreplace(String str, char oldchar, char newchar);

	public String strreplace(String str, String oldstr, String newstr);

	public String strreplace(String str, int start, int end, String with);

	public String strindexOf(String str, char ch);

	public String strindexOf(String str, String strsr);

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
}
