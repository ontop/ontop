package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.*;

public class OracleSQLDialectAdapter extends SQL99DialectAdapter {

	public static final int NAME_MAX_LENGTH = 30;
	/**
	 * If the name (of a variable/view) needs to be shortcut, length of the number
	 * introduced.
	 */
	public static final int NAME_NUMBER_LENGTH = 3;

	private String databaseVersion ;

	public OracleSQLDialectAdapter() {
		this.databaseVersion = "";

	}

	public OracleSQLDialectAdapter(String databaseVersion) {
		this.databaseVersion = databaseVersion;

	}

	/**
	 * Versions < 12.1 are not supported
	 *
	 * Reason: In 12.1 and later, you can use the OFFSET and/or FETCH [FIRST | NEXT] operators
	 */
	@Override
	public String sqlSlice(long limit, long offset) {

		if ((limit < 0) && (offset < 0))
			return "";

		String version = databaseVersion.split("\\.")[0];
		try {
			int versionInt = Integer.parseInt(version);
//
			if (versionInt < 12) {
				throw new UnsupportedOperationException("LIMIT and OFFSET are not supported " +
						"for Oracle DBs prior to 12.1");
//					if (limit == 0) {
//						return "WHERE 1 = 0";
//					}
//
//					if (limit < 0) {
//						if (offset < 0)
//						{
//							return "";
//						} else
//						{
//
//							return String.format("OFFSET %d ROWS", offset);
//						}
//					}
//					else if (limit > 0 )
//						return String.format("WHERE ROWNUM <= %s", limit);
//					else
//						// TODO: support offset!
//						return "";
//					}
			}
		}
		/*
		 * Happens also when the DB version is undefined
		 */
		catch (NumberFormatException nfe) {
		}

		if (offset <= 0) {
			// If the offset is not specified
			return String.format("FETCH NEXT %d ROWS ONLY", limit);
		} else if (limit < 0) {
			return String.format("OFFSET %d ROWS\nFETCH NEXT 99999999 ROWS ONLY", limit);
		}
		else {
			return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
		}
	}

	@Override
	public Optional<String> getTrueTable() {
		return Optional.of("dual");
	}

	@Override
	public String getSQLLexicalFormBoolean(boolean value) {
		return value ? 	"1" : "0";
	}
	
	/***
	 * Given an XSD dateTime this method will generate a SQL TIMESTAMP value.
	 * The method will strip any fractional seconds found in the date time
	 * (since we haven't found a nice way to support them in all databases). It
	 * will also normalize the use of Z to the timezome +00:00 and last, if the
	 * database is H2, it will remove all timezone information, since this is
	 * not supported there.
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
		if (bf.length() > 19) {
			bf.delete(19, bf.length());
		}
		bf.insert(0, "'");
		bf.append("'");
		
		/*
		 * Oracle has a special treatment for datetime datatype such that it requires a default
		 * datetime format. In this case, the default is 'YYYY-MM-DD HH24:MI:SS.FF' as in SPARQL
		 * standard, e.g., to_date('2012-12-18 09:58:23.2','YYYY-MM-DD HH24:MI:SS.FF')
		 */
		bf.insert(0, "to_timestamp(");
		bf.append(",'YYYY-MM-DD HH24:MI:SS')");
			
		return bf.toString();
	}

	@Override
	public String nameTopVariable(String signatureVariableName, Set<String> sqlVariableNames) {
		return nameViewOrVariable("", signatureVariableName, "", sqlVariableNames, true);
	}

	@Override
	public String render(DBConstant constant) {
		DBTermType dbType = constant.getType();
		switch (dbType.getCategory()) {
			case DATETIME:
				return String.format("TIMESTAMP '%s'", constant.getValue());
			default:
				return super.render(constant);
		}
	}

	/**
	 * Makes sure the view or variable name never exceeds the max length supported by Oracle.
	 *
	 * Strategy: shortens the intermediateName and introduces a number to avoid conflict with
	 * similar names.
	 */
	private String nameViewOrVariable(final String prefix,
									  final String intermediateName,
									  final String suffix,
									  final Collection<String> alreadyDefinedNames,
									  boolean putQuote) {
		int borderLength = prefix.length() + suffix.length();
		int signatureVarLength = intermediateName.length();

		if (borderLength >= (NAME_MAX_LENGTH - NAME_NUMBER_LENGTH))  {
			throw new IllegalArgumentException("The prefix and the suffix are too long (their accumulated length must " +
					"be less than " + (NAME_MAX_LENGTH - NAME_NUMBER_LENGTH) + ")");
		}

		/*
		 * If the length limit is not reached, processes as usual.
		 */
		if (signatureVarLength + borderLength <= NAME_MAX_LENGTH) {
			String unquotedName = buildDefaultName(prefix, intermediateName, suffix);
			String name = putQuote ? sqlQuote(unquotedName) : unquotedName;
			return name;
		}

		String shortenIntermediateNamePrefix = intermediateName.substring(0, NAME_MAX_LENGTH - borderLength
				- NAME_NUMBER_LENGTH);

		/*
		 * Naive implementation
		 */
		for (int i = 0; i < Math.pow(10, NAME_NUMBER_LENGTH); i++) {
			String unquotedVarName = buildDefaultName(prefix, shortenIntermediateNamePrefix + i, suffix);
			String mainVarName = putQuote ? sqlQuote(unquotedVarName) : unquotedVarName;
			if (!alreadyDefinedNames.contains(mainVarName)) {
				return mainVarName;
			}
		}

		// TODO: find a better exception
		throw new RuntimeException("Impossible to create a new variable/view " + prefix + shortenIntermediateNamePrefix
				+ "???" + suffix + " : already " + Math.pow(10, NAME_NUMBER_LENGTH) + " of them.");
	}
}
