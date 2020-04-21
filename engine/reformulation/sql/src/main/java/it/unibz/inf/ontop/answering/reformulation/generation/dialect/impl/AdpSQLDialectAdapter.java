package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;



public class AdpSQLDialectAdapter extends SQL99DialectAdapter {
	
	/**
	 * same as PostgreSQL
	 * sqlLimit and getTopNSQL are standard
	 */
	@Override
	public String sqlOffset(long offset) {
		return String.format("LIMIT ALL\nOFFSET %d", offset);
	}

	@Override
	public String sqlLimitOffset(long limit, long offset) {
		return String.format("LIMIT %d\nOFFSET %d", limit, offset);
	}

}
