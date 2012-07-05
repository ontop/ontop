package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;

import java.util.List;

public interface SQLDialectAdapter {

	public String strconcat(String[] strings);

	public String strreplace(String str, char oldchar, char newchar);

	public String strreplace(String str, String oldstr, String newstr);

	public String strreplace(String str, int start, int end, String with);

	public String strindexOf(String str, char ch);
	
	public String strindexOf(String str, String strsr);
	/*
	 * Table/Column name functions
	 */
	
	public String sqlQualifiedColumn(String tablename, String columnname);

	public String sqlTableName(String tablename, String viewname);

	public String sqlQuote(String name);
	
	public String sqlSlice(long limit, long offset);
	
	public String sqlOrderBy(List<OrderCondition> conditions, String viewname);
}
