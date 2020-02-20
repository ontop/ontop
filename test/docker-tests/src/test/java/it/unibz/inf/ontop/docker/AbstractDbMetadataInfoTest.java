package it.unibz.inf.ontop.docker;

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

import it.unibz.inf.ontop.dbschema.RDBMetadata;
import it.unibz.inf.ontop.dbschema.RDBMetadataExtractionTools;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public abstract class AbstractDbMetadataInfoTest extends TestCase {
	
	private RDBMetadata metadata;
	private String propertyFile;
	private Properties properties;

	private static Logger log = LoggerFactory.getLogger(AbstractDbMetadataInfoTest.class);

	public AbstractDbMetadataInfoTest(String propertyFile) {
		this.propertyFile = propertyFile;

	}

	@Override
	public void setUp() throws Exception {
		
		try {
			InputStream pStream =this.getClass().getResourceAsStream(propertyFile);
			properties = new Properties();
			properties.load(pStream);
			Connection conn = DriverManager.getConnection(getConnectionString(), getConnectionUsername(), getConnectionPassword());

			OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();

			metadata = RDBMetadataExtractionTools.createMetadata(conn, defaultConfiguration.getTypeFactory().getDBTypeFactory());

			RDBMetadataExtractionTools.loadMetadata(metadata, defaultConfiguration.getTypeFactory().getDBTypeFactory(), conn, null);
		}
		catch (IOException e) {
			log.error("IOException during setUp of propertyFile");
			e.printStackTrace();
		}
		catch (SQLException e) {
			log.error("SQL Exception during setUp of metadata");
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
			log.info(msg);
		}
	}

	public String getConnectionPassword() {
		return properties.getProperty("jdbc.password");
	}


	public String getConnectionString() {
		return properties.getProperty("jdbc.url");
	}


	public String getConnectionUsername() {
		return properties.getProperty("jdbc.user");
	}


	public String getDriverName() {
		return properties.getProperty("jdbc.driver");
	}
}
