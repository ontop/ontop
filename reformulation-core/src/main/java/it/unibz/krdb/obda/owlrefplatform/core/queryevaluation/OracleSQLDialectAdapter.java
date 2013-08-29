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
import java.util.HashMap;
import java.util.Map;

public class OracleSQLDialectAdapter extends SQL99DialectAdapter {

	private static Map<Integer, String> SqlDatatypes;
	static {
		SqlDatatypes = new HashMap<Integer, String>();
		SqlDatatypes.put(Types.DECIMAL, "NUMBER");
		SqlDatatypes.put(Types.FLOAT, "FLOAT");
		SqlDatatypes.put(Types.CHAR, "CHAR");
		SqlDatatypes.put(Types.VARCHAR, "VARCHAR(4000)");
		SqlDatatypes.put(Types.CLOB, "CLOB");
		SqlDatatypes.put(Types.TIMESTAMP, "TIMESTAMP");
	}
	
	@Override
	public String sqlSlice(long limit, long offset) {
		return String.format("WHERE ROWNUM <= %s", limit);
	}

	@Override
	public String sqlCast(String value, int type) {
		String strType = SqlDatatypes.get(type);
		if (strType != null) {	
			return "CAST(" + value + " AS " + strType + ")";
		}
		throw new RuntimeException("Unsupported SQL type");
	}
}
