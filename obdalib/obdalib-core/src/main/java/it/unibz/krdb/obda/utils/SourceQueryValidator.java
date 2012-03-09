/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SourceQueryValidator {

	private OBDAQuery sourceQuery = null;
	// private IncrementalResultSetTableModel model = null;

	private Exception reason = null;

	JDBCConnectionManager modelfactory = null;

	OBDADataSource source = null;

	private Statement st;

	private Connection c;

	public SourceQueryValidator(OBDADataSource source, OBDAQuery q) {
		this.source = source;
		sourceQuery = q;
	}

	// public Object execute() {
	//
	// if (model != null) {
	// return model;
	// } else {
	// if (validate()) {
	// return model;
	// } else {
	// return null;
	// }
	// }
	// }

	public boolean validate() {
		ResultSet set = null;
		try {
			modelfactory = JDBCConnectionManager.getJDBCConnectionManager();

			// if (model != null) {
			//
			// IncrementalResultSetTableModel rstm = model;
			// rstm.close();
			// }
			c = modelfactory.getConnection(source);
			st = c.createStatement();
			set = st.executeQuery(sourceQuery.toString());
			// model = new IncrementalResultSetTableModel(set);

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
			}
			try {
				st.close();
			} catch (Exception e) {
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

	// public void dispose() {
	//
	// if (model != null)
	// model.close();
	// }

}
