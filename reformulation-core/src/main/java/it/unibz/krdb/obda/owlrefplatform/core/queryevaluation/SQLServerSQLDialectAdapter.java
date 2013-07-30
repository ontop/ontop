/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

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
		if (limit == Long.MIN_VALUE || limit == 0) {
			if (offset == Long.MIN_VALUE) {
				return "OFFSET 0 ROWS";
			} else {
				return String.format("OFFSET %d ROWS", offset);
			}
		} else {
			if (offset == Long.MIN_VALUE) {
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
