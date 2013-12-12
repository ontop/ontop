package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class Mysql2SQLDialectAdapter extends SQL99DialectAdapter {

	private static Map<Integer, String> SqlDatatypes;
	static {
		SqlDatatypes = new HashMap<Integer, String>();
		SqlDatatypes.put(Types.INTEGER, "INT");
		SqlDatatypes.put(Types.DECIMAL, "DECIMAL");
		SqlDatatypes.put(Types.REAL, "FLOAT");
		SqlDatatypes.put(Types.DOUBLE, "DOUBLE");
		SqlDatatypes.put(Types.CHAR, "CHAR");
		SqlDatatypes.put(Types.VARCHAR, "CHAR(8000) CHARACTER SET utf8");  // for korean, chinese, etc characters we need to use utf8
		SqlDatatypes.put(Types.DATE, "DATE");
		SqlDatatypes.put(Types.TIME, "TIME");
		SqlDatatypes.put(Types.TIMESTAMP, "DATETIME");
	}
	
	@Override
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		if (strings.length == 1)
			return strings[0];
		
		StringBuilder sql = new StringBuilder();

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
		if (limit < 0 || limit == 0) {
			if (offset < 0) {
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
			if (offset < 0) {
				// If the offset is not specified
				return String.format("LIMIT %d", limit);
			} else {
				return String.format("LIMIT %d,%d", offset, limit);
			}
		}
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
