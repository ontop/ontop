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
import it.unibz.krdb.obda.model.Variable;

import java.sql.Types;
import java.util.List;

public class SQL99DialectAdapter implements SQLDialectAdapter {

	@Override
	public String strconcat(String[] strings) {
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
	public String strreplace(String str, char oldchar, char newchar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strreplace(String str, String oldstr, String newstr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strreplace(String str, int start, int end, String with) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strindexOf(String str, char ch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strindexOf(String str, String strsr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sqlQualifiedColumn(String tablename, String columnname) {
		// TODO: This should depend on whether the column name was quoted in the original sql query
		return String.format("%s.\"%s\"", tablename, columnname);
	}

	
	@Override
/*	public String sqlTableName(String tablename, String viewname) {
		return String.format("\"%s\" %s", tablename, viewname);
	}*/
	
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

	@Override
	public String sqlSlice(long limit, long offset) {
		// TODO Auto-generated method stub
		return null;
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
	public String sqlGroupBy(List<Variable> groupby, String viewname) {
		String sql = "GROUP BY ";
		boolean needComma = false;
		for (Variable v : groupby) {
			if (needComma) {
				sql += ", ";
			}
			//sql += sqlQualifiedColumn(viewname, v.getName());
			sql += String.format("\"%s\"", v.getName());
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
		pattern = pattern.substring(1, pattern.length() - 1); // remove the
																// enclosing
																// quotes
		//we use % wildcards to search for a string that contains and not only match the pattern
		if (caseinSensitive) {
			return " LOWER(" + columnname + ") LIKE " + "'%"
					+ pattern.toLowerCase() + "%'";
		}
		return columnname + " LIKE " + "'%" + pattern + "%'";
	}

	@Override
	public String sqlRegex(String columnname, String pattern,
			boolean caseinSensitive) {
		// TODO Auto-generated method stub
		return null;
	}
}
