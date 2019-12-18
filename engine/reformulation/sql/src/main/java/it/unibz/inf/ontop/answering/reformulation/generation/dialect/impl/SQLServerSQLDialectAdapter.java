package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

import java.util.Optional;

public class SQLServerSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		if(limit == 0){
			return "WHERE 1 = 0";
		}

		if (limit < 0)  {
			if (offset < 0 ) {
				return "";
			} else {

				return String.format("OFFSET %d ROWS", offset);
			}
		} else {
			if (offset < 0) {
				// If the offset is not specified
				return String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit);
			} else {
				return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
			}
		}
	}

	public String sqlLimit(String originalString, long limit) {
		final String limitStmt = String.format("TOP %d ", limit);
		StringBuilder sb = new StringBuilder(originalString);
		int insertPosition = originalString.indexOf(" ") + 1;
		sb.insert(insertPosition, limitStmt);
		return sb.toString();
	}

	@Override
	public Optional<String> getTrueTable() {
		return Optional.of("\"example\"");
	}



}
