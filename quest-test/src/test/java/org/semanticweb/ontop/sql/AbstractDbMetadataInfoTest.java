package org.semanticweb.ontop.sql;

/*
 * #%L
 * ontop-test
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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDbMetadataInfoTest extends TestCase {
	
	private DBMetadata metadata;
	
	private static Logger log = LoggerFactory.getLogger(AbstractDbMetadataInfoTest.class);
	
	@Override
	public void setUp() {
		try {
			Class.forName(getDriverName());
		} 
		catch (ClassNotFoundException e) { /* NO-OP */ }
		
		try {
			Connection conn = DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());
			metadata = JDBCConnectionManager.getMetaData(conn);
		} catch (SQLException e) { 
			e.printStackTrace();
		}
	}
	
	public void testPropertyInfo() throws SQLException {
		final Driver driver = DriverManager.getDriver(getConnectionString());

		DriverPropertyInfo[] propInfo = null;
		try {
			propInfo = driver.getPropertyInfo(getConnectionString(), null);
		} catch (final RuntimeException err) {
			// Some drivers (Sun's ODBC-JDBC) throw null pointer exceptions ...
			// Try again, but with an empty properties ...
			try {
				propInfo = driver.getPropertyInfo(getConnectionString(), new Properties());
			} catch (final RuntimeException err2) {
				// Okay, give up
			}
		}

		for (final DriverPropertyInfo info : propInfo) {
			StringBuilder choices = new StringBuilder();
			if (info.choices != null) {
				choices.append("[");
				boolean needComma = false;
				for (String opt : info.choices) {
					if (needComma) {
						choices.append(", ");
					}
					choices.append(opt);
					needComma = true;
				}
				choices.append("]");
			}
			String msg = String.format("%s : %s : %s : %s : %s", info.name, info.value, choices.toString(), info.required, info.description);
			System.out.println(msg);
		}
	}
	
	protected abstract String getDriverName();
	protected abstract String getConnectionString();
	protected abstract String getConnectionUsername();
	protected abstract String getConnectionPassword();
}
