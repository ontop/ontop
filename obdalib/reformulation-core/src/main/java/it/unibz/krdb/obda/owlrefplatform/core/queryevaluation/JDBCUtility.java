package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

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
	private enum Driver {
		PGSQL, MYSQL, H2, DB2, ORACLE, TEIID
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
			sql = String.format("CONCAT('%s'", uribase);
			for (int i = 0; i < list.size(); i++) {
				sql += String.format(", '-', %s", list.get(i));
			}
			sql += ")";
			break;
		case PGSQL:
		case ORACLE:
		case H2:
		case TEIID:
			sql = String.format("('%s'", uribase);
			for (int i = 0; i < list.size(); i++) {
				sql += String.format("|| '-' || %s", list.get(i));
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
		}
		return sql;
	}
}
