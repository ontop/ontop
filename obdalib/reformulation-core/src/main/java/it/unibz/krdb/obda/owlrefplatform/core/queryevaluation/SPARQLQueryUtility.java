package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

public class SPARQLQueryUtility {
	
	private String query;
	
	private static final String ASK_KEYWORD = "ask";
	private static final String SELECT_KEYWORD = "select";
	private static final String CONSTRUCT_KEYWORD = "construct";
	private static final String DESCRIBE_KEYWORD = "describe";
	
	public SPARQLQueryUtility(String query) {
		this.query = query;
	}
	
	public String getQueryString() {
		return query;
	}
	
	public boolean isAskQuery() {
		return query.toLowerCase().contains(ASK_KEYWORD);
	}
	
	public boolean isSelectQuery() {
		return query.toLowerCase().contains(SELECT_KEYWORD);
	}
	
	public boolean isConstructQuery() {
		return query.toLowerCase().contains(CONSTRUCT_KEYWORD);
	}
	
	public boolean isDescribeQuery() {
		return query.toLowerCase().contains(DESCRIBE_KEYWORD);
	}
}