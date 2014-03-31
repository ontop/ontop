package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;

public class HSQLSQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String sqlSlice(long limit, long offset) {
		if (limit < 0 || limit == 0) {
			if (offset < 0) {
				/* If both limit and offset is not specified.
				 */
				return "LIMIT 0";
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
	@Override
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR(500)";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
		//return value;
	}

	

	@Override
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {
		pattern = pattern.substring(1, pattern.length() - 1); // remove the
																// enclosing
																// quotes
		// embedded options: 
		String hsqlpat= pattern.replace("\\.", ".");
		if (caseinSensitive){
			columnname = "LCASE("+columnname+")";
			hsqlpat= hsqlpat.toLowerCase();
		}
		
		
		String statement = columnname + " LIKE " + "'%" +  hsqlpat  + "%'";
		return statement;
	}
	
}
