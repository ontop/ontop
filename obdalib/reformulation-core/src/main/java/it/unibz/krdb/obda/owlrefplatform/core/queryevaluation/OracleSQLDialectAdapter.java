package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

public class OracleSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		return String.format("ROWNUM <= %s", limit);
	}
}
