package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;

public class DB2SQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		if (strings.length == 1)
			return strings[0];
		StringBuffer sql = new StringBuffer();

		sql.append(String.format("(%s", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(" || %s", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}

		@Override
		public String sqlSlice(long limit, long offset) {
			if (limit == Long.MIN_VALUE || limit == 0) {
				if (offset == Long.MIN_VALUE) {
					// If both limit and offset is not specified.
					return "";
				} else {
					// The max number of rows is specified by the development team.
					return String.format("LIMIT 8000\nOFFSET %d", offset);
				}
			} else {
				if (offset == Long.MIN_VALUE) {
					// If the offset is not specified
					return String.format("LIMIT %d\n", limit);
				} else {
					return String.format("LIMIT %d\nOFFSET %d", limit, offset);
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
}
