package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class H2SQLDialectAdapter extends SQL99DialectAdapter {

	/**
	 * Number of rows in output can be limited either with standard OFFSET / FETCH,
	 * with non-standard LIMIT / OFFSET, or with non-standard TOP clauses.
	 * Different clauses cannot be used together. OFFSET specifies how many rows to skip.
	 * Please note that queries with high offset values can be slow.
	 * FETCH FIRST/NEXT, LIMIT or TOP limits the number of rows returned by the query
	 * (no limit if null or smaller than zero). If PERCENT is specified number of rows
	 * is specified as a percent of the total number of rows and should be an integer
	 * value between 0 and 100 inclusive. WITH TIES can be used only together with
	 * ORDER BY and means that all additional rows that have the same sorting position
	 * as the last row will be also returned.
	 *
	 * LIMIT expression [OFFSET expression]
	 * OFFSET expression ROW|ROWS FETCH FIRST|(NEXT expression) [PERCENT]
	 */

	// sqlLimit and getTopNSQL are standard

	@Override
	public String sqlLimitOffset(long limit, long offset) {
		if (limit == 0)
			return "LIMIT 0";
		return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
	}

	@Override
	public String sqlOffset(long offset) {
		return String.format("OFFSET %d ROWS", offset);
	}
}
