package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;



public class AdpSQLDialectAdapter extends SQL99DialectAdapter {
	
	/**
	 * same as PostgreSQL
	 *
	 */
	@Override
	public String sqlOffset(long offset) {
		return String.format("LIMIT ALL\nOFFSET %d", offset);
	}

	@Override
	public String sqlLimit(long limit) {
		return String.format("LIMIT %d\nOFFSET 0", limit);
	}

	@Override
	public String sqlLimitOffset(long limit, long offset) {
		return String.format("LIMIT %d\nOFFSET %d", limit, offset);
	}

	@Override
	public String getTopNSQL(String sqlString, int top) {
		String slice = String.format("LIMIT %d\nOFFSET 0", top);
		return String.format("%s %s", sqlString, slice);
	}

}
