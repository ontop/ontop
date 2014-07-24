package org.semanticweb.ontop.owlrefplatform.core.queryevaluation;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLAdapterFactory {

	private static Logger log = LoggerFactory.getLogger(SQLAdapterFactory.class);

	public static SQLDialectAdapter getSQLDialectAdapter(String className) {

		if (className.equals("org.postgresql.Driver")) {
			return new PostgreSQLDialectAdapter();
		} else if (className.equals("com.mysql.jdbc.Driver")) {
			return new Mysql2SQLDialectAdapter();
		} else if (className.equals("org.h2.Driver")) {
			return new H2SQLDialectAdapter();
		} else if (className.equals("com.ibm.db2.jcc.DB2Driver")) {
			return new DB2SQLDialectAdapter();
		} else if (className.equals("oracle.jdbc.driver.OracleDriver") || className.equals("oracle.jdbc.OracleDriver")) {
			return new OracleSQLDialectAdapter();
		} else if (className.equals("org.teiid.jdbc.TeiidDriver")) {
			return new TeiidSQLDialectAdapter();
		} else if (className.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")) {
			return new SQLServerSQLDialectAdapter();
		} else if (className.equals("org.hsqldb.jdbc.JDBCDriver")) {
			return new HSQLSQLDialectAdapter();
		} else if (className.equals("madgik.adp.federatedjdbc.AdpDriver")){
			return new AdpSQLDialectAdapter();
		} else {
			log.warn("WARNING: the specified driver doesn't correspond to any of the drivers officially supported by Ontop.");
			log.warn("WARNING: Contact the authors for further support.");
			return new SQL99DialectAdapter();
		}

		

	}

}
