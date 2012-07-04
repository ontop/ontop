package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

public class OracleSQLDialectAdapter extends SQL99DialectAdapter {

	
	
	@Override
	public String sqlLimit(int limit) {
		return String.format("ROWNUM <= %s", limit);
	}

}
