package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

public class PostgreSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit == Long.MIN_VALUE || limit == 0) {
			// if the limit is not specified
			return String.format("LIMIT ALL\nOFFSET %d", offset);
		} else {
			if (offset == Long.MIN_VALUE) {
				// If the offset is not specified
				return String.format("LIMIT %d\nOFFSET 0", limit);
			} else {
				return String.format("LIMIT %d\nOFFSET %d", limit, offset);
			}
		}
	}
}
