package org.semanticweb.ontop.mapping.sql;

/*
 * #%L
 * ontop-obdalib-core
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.sql.JDBCConnectionManager;

public class SQLSourceQueryValidator {

	private OBDAQuery sourceQuery = null;

	private Exception reason = null;

	private JDBCConnectionManager modelfactory = null;

	private OBDADataSource source = null;

	private Statement st;

	private Connection c;

	public SQLSourceQueryValidator(OBDADataSource source, OBDAQuery q) {
		this.source = source;
		sourceQuery = q;
	}

	public boolean validate() {
		ResultSet set = null;
		try {
			modelfactory = JDBCConnectionManager.getJDBCConnectionManager();
			c = modelfactory.getConnection(source);
			st = c.createStatement();
			set = st.executeQuery(sourceQuery.toString());
			return true;
		} catch (SQLException e) {
			reason = e;
			return false;
		} catch (Exception e) {
			reason = e;
			return false;
		} finally {
			try {
				set.close();
			} catch (Exception e) {
				// NO-OP
			}
			try {
				st.close();
			} catch (Exception e) {
				// NO-OP
			}
		}
	}

	public void cancelValidation() throws SQLException {
		st.cancel();
	}

	/***
	 * Returns the exception that cause the query to be invalid.
	 * 
	 * @return Exception that caused invalidity. null if no reason was set or if
	 *         the query is valid.
	 */
	public Exception getReason() {
		return reason;
	}
}
