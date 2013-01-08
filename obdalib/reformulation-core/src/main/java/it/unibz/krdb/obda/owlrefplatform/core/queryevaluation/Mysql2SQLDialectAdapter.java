package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;

public class Mysql2SQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		if (strings.length == 1)
			return strings[0];
		
		StringBuffer sql = new StringBuffer();

		sql.append(String.format("CONCAT(%s", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(", %s", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}
	
	@Override
	public String sqlQualifiedColumn(String tablename, String columnname) {
		return String.format("%s.`%s`", tablename, columnname);
	}

	@Override
	public String sqlTableName(String tablename, String viewname) {
		return String.format("`%s` %s", tablename, viewname);
	}

	@Override
	public String sqlQuote(String name) {
		return String.format("`%s`", name);
	}

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit == Long.MIN_VALUE || limit == 0) {
			if (offset == Long.MIN_VALUE) {
				/* If both limit and offset is not specified.
				 */
				return "LIMIT 0";
			} else {
				/* If the limit is not specified then put a big number as suggested 
				 * in http://dev.mysql.com/doc/refman/5.0/en/select.html
				 */
				return String.format("LIMIT %d,18446744073709551615", offset);
			}
		} else {
			if (offset == Long.MIN_VALUE) {
				// If the offset is not specified
				return String.format("LIMIT %d", limit);
			} else {
				return String.format("LIMIT %d,%d", offset, limit);
			}
		}
	}
}
