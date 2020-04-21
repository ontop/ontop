package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class SQLServerSQLDialectAdapter extends SQL99DialectAdapter {

	/**
	 *  https://docs.microsoft.com/en-us/sql/t-sql/queries/select-order-by-clause-transact-sql?view=sql-server-ver15
	 *
	 * <offset_fetch> ::=
	 * {
	 *     OFFSET { integer_constant | offset_row_count_expression } { ROW | ROWS }
	 *     [
	 *       FETCH { FIRST | NEXT } {integer_constant | fetch_row_count_expression } { ROW | ROWS } ONLY
	 *     ]
	 * }
	 */

	@Override
	public String sqlLimitOffset(long limit, long offset) {
		return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
	}

	@Override
	public String sqlLimit(long limit) {
		return String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit);
	}

	@Override
	public String sqlOffset(long offset) {
		return String.format("OFFSET %d ROWS", offset);
	}


	@Override
	public String getTopNSQL(String sqlString, int limit) {
		String sqlLimit = String.format("SELECT TOP %d ", limit);
		return sqlString.replaceFirst("SELECT ", sqlLimit);
	}



}
