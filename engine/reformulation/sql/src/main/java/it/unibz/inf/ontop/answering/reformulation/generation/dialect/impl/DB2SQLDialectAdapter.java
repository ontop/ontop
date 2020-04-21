package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class DB2SQLDialectAdapter extends SQL99DialectAdapter {

	//sqlLimit and getTopNSQL are standard

	@Override
	public String sqlLimitOffset(long limit, long offset) {
		return String.format("LIMIT %d\nOFFSET %d", limit, offset);
	}

	@Override
	public String sqlOffset(long offset) {
		return sqlLimitOffset(8000, offset);
	}
}
