package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

public class PostgreSQLDialectAdapter extends SQL99DialectAdapter {

	/**
	 * https://www.postgresql.org/docs/8.1/queries-limit.html
	 *
	 * [LIMIT { number | ALL }] [OFFSET number]
	 *
	 * If a limit count is given, no more than that many rows will be returned
	 * (but possibly less, if the query itself yields less rows).
	 * LIMIT ALL is the same as omitting the LIMIT clause.
	 *
	 * OFFSET says to skip that many rows before beginning to return rows.
	 * OFFSET 0 is the same as omitting the OFFSET clause. If both OFFSET and LIMIT
	 * appear, then OFFSET rows are skipped before starting to count the LIMIT rows
	 * that are returned.
	 */

	// sqlLimit, sqlOffset and sqlTopNSQL are standard

	@Override
	public String sqlLimitOffset(long limit, long offset) {
		return String.format("LIMIT %d\nOFFSET %d", limit, offset);
	}
}
