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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OracleSQLDialectAdapter extends SQL99DialectAdapter {

	public static final int NAME_MAX_LENGTH = 30;
	/**
	 * If the name (of a variable/view) needs to be shortcut, length of the number
	 * introduced.
	 */
	public static final int NAME_NUMBER_LENGTH = 3;

	private static Map<Integer, String> SqlDatatypes;
	static {
		SqlDatatypes = new HashMap<Integer, String>();
		SqlDatatypes.put(Types.DECIMAL, "NUMBER");
		SqlDatatypes.put(Types.FLOAT, "FLOAT");
		SqlDatatypes.put(Types.CHAR, "CHAR");
		SqlDatatypes.put(Types.VARCHAR, "VARCHAR(4000)");
		SqlDatatypes.put(Types.CLOB, "CLOB");
		SqlDatatypes.put(Types.TIMESTAMP, "TIMESTAMP");
		SqlDatatypes.put(Types.INTEGER, "INTEGER");
		SqlDatatypes.put(Types.REAL, "NUMBER");
		SqlDatatypes.put(Types.FLOAT, "NUMBER");
		SqlDatatypes.put(Types.DOUBLE, "NUMBER");
//		SqlDatatypes.put(Types.DOUBLE, "DECIMAL"); // it fails aggregate test with double
		SqlDatatypes.put(Types.DATE, "TIMESTAMP");
		SqlDatatypes.put(Types.TIME, "TIME");
		SqlDatatypes.put(Types.BOOLEAN, "BOOLEAN");
	}
	
	@Override
	public String sqlSlice(long limit, long offset) {
		return String.format("WHERE ROWNUM <= %s", limit);
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
	
	@Override
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {
		pattern = pattern.substring(1, pattern.length() - 1); // remove the
																// enclosing
																// quotes
		String flags = "";
		if(caseinSensitive)
			flags += "i";
		else
			flags += "c";
		if (multiLine)
			flags += "m";
		if(dotAllMode)
			flags += "n";
		
		String sql = " REGEXP_LIKE " + "( " + columnname + " , '" + pattern + "' , '" + flags  + "' )";
		return sql;
	}


	@Override
	public String nameTopVariable(String signatureVariableName, String suffix, Set<String> sqlVariableNames) {
		return nameViewOrVariable("", signatureVariableName, suffix, sqlVariableNames, true);
	}

	@Override
	public String nameView(String prefix, String tableName, String suffix, Collection<String> viewNames) {
		return nameViewOrVariable(prefix, tableName, suffix, viewNames, false);
	}

	/**
	 * Makes sure the view or variable name never exceeds the max length supported by Oracle.
	 *
	 * Strategy: shortens the intermediateName and introduces a number to avoid conflict with
	 * similar names.
	 */
	private String nameViewOrVariable(final String prefix,
									  final String intermediateName,
									  final String suffix,
									  final Collection<String> alreadyDefinedNames,
									  boolean putQuote) {
		int borderLength = prefix.length() + suffix.length();
		int signatureVarLength = intermediateName.length();

		if (borderLength >= (NAME_MAX_LENGTH - NAME_NUMBER_LENGTH))  {
			throw new IllegalArgumentException("The prefix and the suffix are too long (their accumulated length must " +
					"be less than " + (NAME_MAX_LENGTH - NAME_NUMBER_LENGTH) + ")");
		}

		/**
		 * If the length limit is not reached, processes as usual.
		 */
		if (signatureVarLength + borderLength <= NAME_MAX_LENGTH) {
			String unquotedName = buildDefaultName(prefix, intermediateName, suffix);
			String name = putQuote ? sqlQuote(unquotedName) : unquotedName;
			return name;
		}

		String shortenIntermediateNamePrefix = intermediateName.substring(0, NAME_MAX_LENGTH - borderLength
				- NAME_NUMBER_LENGTH);

		/**
		 * Naive implementation
		 */
		for (int i = 0; i < Math.pow(10, NAME_NUMBER_LENGTH); i++) {
			String unquotedVarName = buildDefaultName(prefix, shortenIntermediateNamePrefix + i, suffix);
			String mainVarName = putQuote ? sqlQuote(unquotedVarName) : unquotedVarName;
			if (!alreadyDefinedNames.contains(mainVarName)) {
				return mainVarName;
			}
		}

		// TODO: find a better exception
		throw new RuntimeException("Impossible to create a new variable/view " + prefix + shortenIntermediateNamePrefix
				+ "???" + suffix + " : already " + Math.pow(10, NAME_NUMBER_LENGTH) + " of them.");
	}
}
