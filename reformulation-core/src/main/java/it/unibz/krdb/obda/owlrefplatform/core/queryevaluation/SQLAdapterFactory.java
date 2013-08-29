/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLAdapterFactory {

	private static Logger log = LoggerFactory.getLogger(JDBCUtility.class);

	public static SQLDialectAdapter getSQLDialectAdapter(String className) {

		if (className.equals("org.postgresql.Driver")) {
			return new PostgreSQLDialectAdapter();
		} else if (className.equals("com.mysql.jdbc.Driver")) {
			return new Mysql2SQLDialectAdapter();
		} else if (className.equals("org.h2.Driver")) {
			return new H2SQLDialectAdapter();
		} else if (className.equals("com.ibm.db2.jcc.DB2Driver")) {
			return new DB2SQLDialectAdapter();
		} else if (className.equals("oracle.jdbc.driver.OracleDriver")) {
			return new OracleSQLDialectAdapter();
		} else if (className.equals("org.teiid.jdbc.TeiidDriver")) {
			return new TeiidSQLDialectAdapter();
		} else if (className.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")) {
			return new SQLServerSQLDialectAdapter();
		}

		log.warn("WARNING: the specified driver doesn't correspond to any of the drivers officially supported by Quest.");
		log.warn("WARNING: Contact the authors for further support.");
		return new SQL99DialectAdapter();

	}

}
