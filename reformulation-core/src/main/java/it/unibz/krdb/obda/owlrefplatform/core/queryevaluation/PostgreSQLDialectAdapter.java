package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;

public class PostgreSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 || limit == 0) {
			if (offset < 0) {
				// If both limit and offset is not specified.
				return "LIMIT 0";
			} else {
				// if the limit is not specified
				return String.format("LIMIT ALL\nOFFSET %d", offset);
			}
		} else {
			if (offset < 0) {
				// If the offset is not specified
				return String.format("LIMIT %d\nOFFSET 0", limit);
			} else {
				return String.format("LIMIT %d\nOFFSET %d", limit, offset);
			}
		}
	}
	
	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR(10485760)";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
	}
}
