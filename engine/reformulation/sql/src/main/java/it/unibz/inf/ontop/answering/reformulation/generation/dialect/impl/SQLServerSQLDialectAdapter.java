package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class SQLServerSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlLimitOffset(long limit, long offset) {
		if (limit == 0)
			return "WHERE 1 = 0";

		return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
	}

	@Override
	public String sqlLimit(long limit) {
		if (limit == 0)
			return "WHERE 1 = 0";

		return String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit);
	}

	@Override
	public String sqlOffset(long offset) {
		return String.format("OFFSET %d ROWS", offset);
	}


	@Override
	public String getTopNSQL(String originalString, int limit) {
		final String limitStmt = String.format("TOP %d ", limit);
		StringBuilder sb = new StringBuilder(originalString);
		int insertPosition = originalString.indexOf(" ") + 1;
		sb.insert(insertPosition, limitStmt);
		return sb.toString();
	}



}
