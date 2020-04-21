package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class Mysql2SQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlLimitOffset(long limit, long offset) {
		return String.format("LIMIT %d,%d", offset, limit);
	}

	@Override
	public String sqlLimit(long limit) {
		return String.format("LIMIT %d", limit);
	}

	@Override
	public String sqlOffset(long offset) {
		/* If the limit is not specified then put a big number as suggested
		 * in http://dev.mysql.com/doc/refman/5.0/en/select.html
		 */
		return String.format("LIMIT %d,18446744073709551615", offset);
	}

	// default
	// public String getTopNSQL(String sqlString, int top) {
	//	String slice = String.format("LIMIT %d", top);
	//	return String.format("%s %s", sqlString, slice);
	// }


	@Override public String escapedSingleQuote(){
		return "\\'";
	 }

	/**
	 * Also doubles anti-slashes
	 */
    @Override
    public String getSQLLexicalFormString(String constant) {
        //return "'" + constant.replace("\\", "\\\\").replace("'", escapedSingleQuote()) + "'";
        return super.getSQLLexicalFormString(constant.replace("\\", "\\\\"));
    }
}
