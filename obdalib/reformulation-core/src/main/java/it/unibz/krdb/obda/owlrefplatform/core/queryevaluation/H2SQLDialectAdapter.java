package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

public class H2SQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit == Long.MIN_VALUE || limit == 0) {
			if (offset == Long.MIN_VALUE) {
				// If both limit and offset is not specified.
				return "LIMIT 0";
			} else {
				// Max limit: http://www.h2database.com/html/advanced.html#limits_limitations
				return String.format("LIMIT %d,2147483647", offset);
			}
		} else {
			if (offset == Long.MIN_VALUE) {
				// If the offset is not specified
				return String.format("LIMIT %d", limit);
			} else {
				return String.format("LIMIT %d,%d", offset, limit);
			}
		}
	}
}
