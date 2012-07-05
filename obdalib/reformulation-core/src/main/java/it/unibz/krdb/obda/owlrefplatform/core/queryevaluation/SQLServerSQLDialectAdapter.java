package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

public class SQLServerSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		
		if (strings.length == 1)
			return strings[0];
		
		StringBuffer sql = new StringBuffer();

		sql.append(String.format("(%s", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(" + CAST(%s as varchar(8000))", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}
	
	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit == Long.MIN_VALUE || limit == 0) {
			// if the limit is not specified
			return String.format("OFFSET %d ROW", offset);
		} else {
			if (offset == Long.MIN_VALUE) {
				// If the offset is not specified
				return String.format("FETCH FIRST %d ROW ONLY", limit);
			} else {
				return String.format("OFFSET %d ROW\nFETCH NEXT %d ROW ONLY", offset, limit);
			}
		}
	}
}
