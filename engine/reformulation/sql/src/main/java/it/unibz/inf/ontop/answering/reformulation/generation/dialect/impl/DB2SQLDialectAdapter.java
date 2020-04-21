package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class DB2SQLDialectAdapter extends SQL99DialectAdapter {


	@Override
	public String sqlLimitOffset(long limit, long offset) {
		return String.format("LIMIT %d\nOFFSET %d", limit, offset);
	}

	@Override
	public String sqlLimit(long limit) {
		return String.format("LIMIT %d\n", limit);
	}

	@Override
	public String sqlOffset(long offset) {
		return String.format("LIMIT 8000\nOFFSET %d", offset);
	}
}
