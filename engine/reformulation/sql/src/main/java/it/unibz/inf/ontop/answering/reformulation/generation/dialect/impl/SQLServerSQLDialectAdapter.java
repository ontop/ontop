package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class SQLServerSQLDialectAdapter extends SQL99DialectAdapter {


	@Override
	public String getTopNSQL(String sqlString, int limit) {
		String sqlLimit = String.format("SELECT TOP %d ", limit);
		return sqlString.replaceFirst("SELECT ", sqlLimit);
	}


}
