package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


public class Mysql2SQLDialectAdapter extends SQL99DialectAdapter {

	@Override
	public String strConcat(String[] strings) {
		if (strings.length == 0)
			throw new IllegalArgumentException("Cannot concatenate 0 strings");
		if (strings.length == 1)
			return strings[0];
		
		StringBuilder sql = new StringBuilder();

		sql.append(String.format("CONCAT(%s", strings[0]));
		for (int i = 1; i < strings.length; i++) {
			sql.append(String.format(", %s", strings[i]));
		}
		sql.append(")");
		return sql.toString();
	}

	/*Now we use the table name given by the user,
	  and we assume that it includes the quotes if needed*/
	@Override
	public String sqlQuote(String name) {
		return String.format("`%s`", name);
	}

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

	@Override 
	public String getSQLLexicalFormBoolean(boolean value) {
		return value ? 	"TRUE" : "FALSE";
	}

	/***
	 * Given an XSD dateTime this method will generate a SQL TIMESTAMP value.
	 * The method will strip any fractional seconds found in the date time
	 * (since we haven't found a nice way to support them in all databases). It
	 * will also normalize the use of Z to the timezome +00:00 and last, if the
	 * database is H2, it will remove all timezone information, since this is
	 * not supported there.
	 * 
	 *
	 * @return
	 */
	@Override
	public String getSQLLexicalFormDatetime(String v) {
		String datetime = v.replace('T', ' ');
		int dotlocation = datetime.indexOf('.');
		int zlocation = datetime.indexOf('Z');
		int minuslocation = datetime.indexOf('-', 10); // added search from 10th pos, because we need to ignore minuses in date
		int pluslocation = datetime.indexOf('+');
		StringBuilder bf = new StringBuilder(datetime);
		if (zlocation != -1) {
			/*
			 * replacing Z by +00:00
			 */
			bf.replace(zlocation, bf.length(), "+00:00");
		}

		if (dotlocation != -1) {
			/*
			 * Stripping the string from the presicion that is not supported by
			 * SQL timestamps.
			 */
			// TODO we need to check which databases support fractional
			// sections (e.g., oracle,db2, postgres)
			// so that when supported, we use it.
			int endlocation = Math.max(zlocation, Math.max(minuslocation, pluslocation));
			if (endlocation == -1) {
				endlocation = datetime.length();
			}
			bf.replace(dotlocation, endlocation, "");
		}
		bf.insert(0, "'");
		bf.append("'");
		
		return bf.toString();
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
