package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class MySQLDialectAdapter extends SQL99DialectAdapter {

	/**
	 *  http://dev.mysql.com/doc/refman/5.0/en/select.html
	 *
	 * With two arguments, the first argument specifies the offset of the first row to return,
	 * and the second specifies the maximum number of rows to return. The offset of the initial
	 * row is 0 (not 1):
	 * SELECT * FROM tbl LIMIT 5,10;  # Retrieve rows 6-15
	 *
	 * To retrieve all rows from a certain offset up to the end of the result set, you can
	 * use some large number for the second parameter. This statement retrieves all rows from
	 * the 96th row to the last:
	 * SELECT * FROM tbl LIMIT 95,18446744073709551615;
	 *
	 * With one argument, the value specifies the number of rows to return from the beginning
	 * of the result set:
	 * SELECT * FROM tbl LIMIT 5;     # Retrieve first 5 rows
	 * In other words, LIMIT row_count is equivalent to LIMIT 0, row_count.
	 */

	// sqlLimitOffset and sqlLimit are standard

	@Override
	public String sqlOffset(long offset) {
		return sqlLimitOffset(Long.MAX_VALUE, offset);
	}


	@Override
	public String escapedSingleQuote(){ return "\\'"; }

	/**
	 * Also doubles anti-slashes
	 */
    @Override
    public String getSQLLexicalFormString(String constant) {
        //return "'" + constant.replace("\\", "\\\\").replace("'", escapedSingleQuote()) + "'";
        return super.getSQLLexicalFormString(constant.replace("\\", "\\\\"));
    }
}
