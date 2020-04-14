package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class Mysql2SQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 ) {
			if (offset < 0) {
				/* If both limit and offset is not specified.
				 */
				return "";
			} else {
				/* If the limit is not specified then put a big number as suggested 
				 * in http://dev.mysql.com/doc/refman/5.0/en/select.html
				 */
				return String.format("LIMIT %d,18446744073709551615", offset);
			}
		} else {
			if (offset < 0) {
				// If the offset is not specified
				return String.format("LIMIT %d", limit);
			} else {
				return String.format("LIMIT %d,%d", offset, limit);
			}
		}
	}

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
