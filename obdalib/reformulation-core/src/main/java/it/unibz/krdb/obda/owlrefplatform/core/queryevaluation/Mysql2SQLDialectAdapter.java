package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

public class Mysql2SQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String strconcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		if (strings.length == 1)
			return strings[0];
		
		StringBuffer sql = new StringBuffer();

		sql.append(String.format("CONCAT(%s", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(", %s", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}

	
	@Override
	public String sqlQualifiedColumn(String tablename, String columnname) {
		return String.format("%s.`%s`", tablename, columnname);
	}

	@Override
	public String sqlTableName(String tablename, String viewname) {
		return String.format("`%s` %s", tablename, viewname);
	}

	@Override
	public String sqlQuote(String name) {
		return String.format("`%s`", name);
	}

}
