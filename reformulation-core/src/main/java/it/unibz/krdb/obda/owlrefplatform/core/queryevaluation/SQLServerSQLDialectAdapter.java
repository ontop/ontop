package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		
		if (strings.length == 1)
			return strings[0];
		
		StringBuilder sql = new StringBuilder();

		sql.append(String.format("(%s", strings[0]));
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
}
