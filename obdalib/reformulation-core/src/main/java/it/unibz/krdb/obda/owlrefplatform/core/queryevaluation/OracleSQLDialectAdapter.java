package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;

public class OracleSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		return String.format("WHERE ROWNUM <= %s", limit);
	}

	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR(4000)";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
	}
}
