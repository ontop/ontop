package it.unibz.inf.ontop.spec.mapping.validation;

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

import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLSourceQueryValidator {

	private final OntopSQLCredentialSettings settings;
	private final SQLPPSourceQuery sourceQuery;

	private Exception reason = null;

	private Statement st;

	public SQLSourceQueryValidator(OntopSQLCredentialSettings settings, SQLPPSourceQuery q) {
		this.settings = settings;
		sourceQuery = q;
	}

	public boolean validate() {
		try {
			JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
			Connection c = man.getConnection(settings);
			st = c.createStatement();
			try (ResultSet rs = st.executeQuery(sourceQuery.toString())) {
				return true;
			}
		}
		catch (Exception e) {
			reason = e;
			return false;
		}
		finally {
			try {
				st.close();
			}
			catch (Exception e) {
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
