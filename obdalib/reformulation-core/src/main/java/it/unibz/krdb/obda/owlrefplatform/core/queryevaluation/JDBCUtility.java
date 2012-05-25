package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JDBC utility class implements the algorithm which is used to over come
 * the impedance mismatch problem, i.e. it manipulates the select statement such
 * that it creates object URIs out of the data values in the way the mappings
 * show it.
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class JDBCUtility implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5218570087742414646L;

	private enum Driver {
		PGSQL, MYSQL, H2, DB2, ORACLE, SQLSERVER, TEIID
	}

	private Driver driver = null;

	Logger log = LoggerFactory.getLogger(JDBCUtility.class);

	/**
	 * Utility class for constructing SQL query string.
	 * 
	 * Note: Right now only pgsql, mysql, h2, db2 and oracle are supported.
	 * Others should follow in the future.
	 * 
	 * @param className
	 *            The driver class name.
	 * @throws Exception
	 */
	public JDBCUtility(String className) throws Exception {
		if (className.equals("org.postgresql.Driver")) {
			driver = Driver.PGSQL;
		} else if (className.equals("com.mysql.jdbc.Driver")) {
			driver = Driver.MYSQL;
		} else if (className.equals("org.h2.Driver")) {
			driver = Driver.H2;
		} else if (className.equals("com.ibm.db2.jcc.DB2Driver")) {
			driver = Driver.DB2;
		} else if (className.equals("oracle.jdbc.driver.OracleDriver")) {
			driver = Driver.ORACLE;
		} else if (className.equals("org.teiid.jdbc.TeiidDriver")) {
			driver = Driver.TEIID;
		} else if (className.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")) {
			driver = Driver.SQLSERVER;
		} else {
			log.warn("WARNING: the specified driver doesn't correspond to any of the drivers officially supported by Quest.");
			log.warn("WARNING: If you database is not fully compliant with SQL 99 you might experience problems using Quest.");
			log.warn("WARNING: Contact the authors for further support.");
			throw new Exception("Unsupported database!");
		}
	}

	/**
	 * Given the URI base and the list of parameters, it contracts the necessary
	 * SQL manipulations depending on the used data source to construct object
	 * URIs.
	 * 
	 * @param uribase
	 *            The base uri specified in the mapping
	 * @param list
	 *            The list of parametes
	 * @return The SQL manipulations to construct a object URI.
	 */
	public String getConcatination(String uribase, List<String> list) {
		String sql = "";

		switch (driver) {
		case MYSQL:
		case DB2:
			sql = String.format("CONCAT(%s", uribase);
			for (int i = 0; i < list.size(); i++) {
				sql += String.format(", %s", list.get(i));
			}
			sql += ")";
			break;
		case PGSQL:
		case ORACLE:
		case H2:
		case TEIID:
			sql = String.format("(%s", uribase);
			for (int i = 0; i < list.size(); i++) {
				sql += String.format(" || %s", list.get(i));
			}
			sql += ")";
			break;
		case SQLSERVER:
			sql = String.format("(%s", uribase);
			for (int i = 0; i < list.size(); i++) {
				sql += String.format(" + CAST(%s as varchar)", list.get(i));
			}
			sql += ")";
			break;
		}
		return sql;
	}

	public String getLimitFunction(int limit) {
		String sql = "";

		switch (driver) {
		case MYSQL:
		case PGSQL:
		case H2:
		case TEIID:
			sql = String.format("LIMIT %s", limit);
			break;
		case DB2:
			sql = String.format("FETCH FIRST %s ROWS ONLY", limit);
			break;
		case ORACLE:
			sql = String.format("ROWNUM <= %s", limit);
			break;
		case SQLSERVER:
			sql = String.format("OFFSET %s ROWS\nFETCH NEXT %s ROWS ONLY ", limit, limit);
		}
		return sql;
	}

	/***
	 * Returns the valid SQL lexical form of rdf literals based on the current
	 * database and the datatype specified in the function predicate.
	 * 
	 * <p>
	 * For example, if the function is xsd:boolean, and the current database is
	 * H2, the SQL lexical form would be for "true" "TRUE" (or any combination
	 * of lower and upper case) or "1" is always
	 * 
	 * @param rdfliteral
	 * @return
	 */
	public String getSQLLexicalForm(Function typedrdfliteral) {
		String sql = null;
		Predicate type = typedrdfliteral.getFunctionSymbol();
		if (type == OBDAVocabulary.XSD_BOOLEAN) {
			ValueConstant c = (ValueConstant) typedrdfliteral.getTerms().get(0);
			sql = getSQLLexicalFormBoolean(c);
		} else {
			sql = ((ValueConstant) typedrdfliteral.getTerms().get(0)).getValue();
		}
		return sql;

	}

	/***
	 * Returns the valid SQL lexical form of rdf literals based on the current
	 * database and the datatype specified in the function predicate.
	 * 
	 * <p>
	 * For example, if the function is xsd:boolean, and the current database is
	 * H2, the SQL lexical form would be for "true" "TRUE" (or any combination
	 * of lower and upper case) or "1" is always
	 * 
	 * @param rdfliteral
	 * @return
	 */
	public String getSQLLexicalForm(ValueConstant constant) {
		String sql = null;
		if (constant.getType() == COL_TYPE.BNODE || constant.getType() == COL_TYPE.LITERAL || constant.getType() == COL_TYPE.OBJECT
				|| constant.getType() == COL_TYPE.STRING) {
			sql = "'" + constant.getValue() + "'";
		} else if (constant.getType() == COL_TYPE.BOOLEAN) {
			sql = getSQLLexicalFormBoolean(constant);
		} else if (constant.getType() == COL_TYPE.DATETIME) {
			sql = getSQLLexicalFormDatetime(constant);
		} else if (constant.getType() == COL_TYPE.DECIMAL || constant.getType() == COL_TYPE.DOUBLE
				|| constant.getType() == COL_TYPE.INTEGER) {
			sql = constant.getValue();
		} else {
			sql = "'" + constant.getValue() + "'";
		}
		return sql;

	}

	public String getSQLLexicalForm(String constant) {
		return "'" + constant + "'";
	}

	/***
	 * Given an XSD dateTime this method will generate a SQL TIMESTAMP value.
	 * The method will strip any fractional seconds found in the date time
	 * (since we haven't found a nice way to support them in all databases). It
	 * will also normalize the use of Z to the timezome +00:00 and last, if the
	 * database is H2, it will remove all timezone information, since this is
	 * not supported there.
	 * 
	 * @param rdfliteral
	 * @return
	 */
	public String getSQLLexicalFormDatetime(ValueConstant rdfliteral) {
		String datetime = rdfliteral.getValue().replace('T', ' ');
		int dotlocation = datetime.indexOf('.');
		int zlocation = datetime.indexOf('Z');
		int minuslocation = datetime.indexOf('-');
		int pluslocation = datetime.indexOf('+');
		StringBuffer bf = new StringBuffer(datetime);
		if (zlocation != 1) {
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
			if (endlocation == -1)
				endlocation = datetime.length();
			bf.replace(dotlocation, endlocation, "");
		}
		if (driver == Driver.H2 && bf.length() > 19) {
			bf.delete(19, bf.length());
		}
		bf.insert(0, "'");
		bf.append("'");
		return bf.toString();
	}

	public String getSQLLexicalFormBoolean(ValueConstant rdfliteral) {
		String value = rdfliteral.getValue().toLowerCase();
		String sql = null;
		if (value.equals("1") || value.equals("true")) {
			switch (driver) {
			case MYSQL:
			case H2:
			case PGSQL:
			case DB2:
			case TEIID:
				sql = "TRUE";
				break;
			case ORACLE:
				sql = "1";
				break;
			case SQLSERVER:
				sql = "'TRUE'";
				break;
			}
		} else if (value.equals("0") || value.equals("false")) {
			switch (driver) {
			case MYSQL:
			case H2:
			case PGSQL:
			case DB2:
			case TEIID:
				sql = "FALSE";
				break;
			case ORACLE:
				sql = "0";
				break;
			case SQLSERVER:
				sql = "'FALSE'";
				break;
			}
		} else {
			throw new RuntimeException("Invalid lexical form for xsd:boolean. Found: " + value);
		}
		return sql;
	}

	public String getQualifiedColumn(String tablename, String columnname) {
		String sql = "";
		switch (driver) {
		case MYSQL:
			sql = String.format("%s.`%s`", tablename, columnname);
			break;
		case H2:
		case PGSQL:
		case TEIID:
		case DB2:
		case ORACLE:
		case SQLSERVER:
			sql = String.format("%s.\"%s\"", tablename, columnname);
		}
		return sql;
	}
	
	public String getTableName(String tablename, String viewname) {
		String sql = "";
		switch (driver) {
		case MYSQL:
			sql = String.format("`%s` %s", tablename, viewname);
			break;
		case H2:
		case PGSQL:
		case TEIID:
		case DB2:
		case ORACLE:
		case SQLSERVER:
			sql = String.format("\"%s\" %s", tablename, viewname);
		}
		return sql;
	}
	
	public String quote(String name) {
		String str = "";
		switch (driver) {
		case MYSQL:
			str = String.format("`%s`", name);
			break;
		case H2:
		case PGSQL:
		case TEIID:
		case DB2:
		case ORACLE:
		case SQLSERVER:
			str = String.format("\"%s\"", name);
		}
		return str;
	}
}
