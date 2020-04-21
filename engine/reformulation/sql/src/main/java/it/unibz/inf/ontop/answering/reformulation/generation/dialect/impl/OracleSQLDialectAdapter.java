package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

public class OracleSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String getTopNSQL(String sqlString, int top) {
		return String.format("SELECT * FROM (%s) WHERE ROWNUM <= %d", sqlString, top);
	}

}
