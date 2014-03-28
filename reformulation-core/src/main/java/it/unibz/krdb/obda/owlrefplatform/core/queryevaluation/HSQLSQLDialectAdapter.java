package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;

public class HSQLSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit == Long.MIN_VALUE || limit == 0) {
			if (offset == Long.MIN_VALUE) {
				// If both limit and offset is not specified.
				return "LIMIT 0 OFFSET 2147483647";
			} else {
				
				return String.format("OFFSET %d", offset);
			}
		} else {
			if (offset == Long.MIN_VALUE) {
				// If the offset is not specified
				return String.format("LIMIT %d", limit);
			} else {
				return String.format("LIMIT %d OFFSET %d", limit, offset);
			}
		}
	}
	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR(500)";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
	}
	@Override
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive) {
		pattern = pattern.substring(1, pattern.length() - 1);
		pattern = pattern.replace("\\", "");
		if (caseinSensitive) {
			return " LOWER(" + columnname + ") LIKE " + "'%"
					+ pattern.toLowerCase() + "%'" + " escape '\\'";
		}
		return columnname + " LIKE " + "'%" + pattern + "%'" + " escape '\\'";
	}
}
