package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SourceQueryValidator {

	private OBDAQuery sourceQuery = null;

	private Exception reason = null;

	private JDBCConnectionManager modelfactory = null;

	private OBDADataSource source = null;

	private Statement st;

	private Connection c;

	public SourceQueryValidator(OBDADataSource source, OBDAQuery q) {
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
