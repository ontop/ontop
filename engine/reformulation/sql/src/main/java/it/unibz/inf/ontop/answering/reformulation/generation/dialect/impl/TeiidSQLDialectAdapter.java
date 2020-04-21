package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

public class TeiidSQLDialectAdapter extends SQL99DialectAdapter {

	/**
	 * See https://docs.jboss.org/teiid/7.7.0.Final/reference/en-US/html/sql_clauses.html#limit_clause
	 */
	@Override
	public String sqlSlice(long limit, long offset) {
		if ((limit < 0) && (offset < 0)) {
			return "";
		}
		else if ((limit >= 0) && (offset >= 0)) {
			return String.format("LIMIT %d, %d", offset, limit);
		}
		else if (offset < 0) {
			return String.format("LIMIT %d", limit);
		}
		// Else -> (limit < 0)
		else {
			return String.format("OFFSET %d ROWS", offset);
		}
	}

	//standard
	//public String getTopNSQL(String sqlString, int top) {
	//	String slice = String.format("LIMIT %d", top);
	//	return String.format("%s %s", sqlString, slice);
	//}
}
