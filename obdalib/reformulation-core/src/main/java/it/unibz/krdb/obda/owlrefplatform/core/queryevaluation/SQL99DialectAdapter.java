package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;

import java.util.List;

public class SQL99DialectAdapter implements SQLDialectAdapter {

	@Override
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		
		if (strings.length == 1)
			return strings[0];
		
		StringBuffer sql = new StringBuffer();

		sql.append(String.format("(%s", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(" || %s", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}

	@Override
	public String strreplace(String str, char oldchar, char newchar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strreplace(String str, String oldstr, String newstr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strreplace(String str, int start, int end, String with) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strindexOf(String str, char ch) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String strindexOf(String str, String strsr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sqlQualifiedColumn(String tablename, String columnname) {
		return String.format("%s.\"%s\"", tablename, columnname);
	}

	@Override
	public String sqlTableName(String tablename, String viewname) {
		return String.format("\"%s\" %s", tablename, viewname);
	}

	@Override
	public String sqlQuote(String name) {
		return String.format("\"%s\"", name);
	}
	
	@Override
	public String sqlSlice(long limit, long offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sqlOrderBy(List<OrderCondition> conditions, String viewname) {
		String sql = "ORDER BY ";
		boolean needComma = false;
		for (OrderCondition c : conditions) {
			if (needComma) {
				sql += ", ";
			}
			sql += sqlQualifiedColumn(viewname, c.getVariable().getName());
			if (c.getDirection() == OrderCondition.ORDER_DESCENDING) {
				sql += " DESC";
			}
			needComma = true;
		}
		return sql;
	}
}
