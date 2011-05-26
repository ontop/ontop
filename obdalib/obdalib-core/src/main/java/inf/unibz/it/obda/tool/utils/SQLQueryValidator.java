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
package inf.unibz.it.obda.tool.utils;

import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.gui.swing.datasource.panels.IncrementalResultSetTableModel;
import inf.unibz.it.obda.gui.swing.exception.NoDatasourceSelectedException;


import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLQueryValidator extends QueryValidator {

	private Query			sourceQuery		= null;
	private IncrementalResultSetTableModel	model			= null;

	private Exception			reason			= null;

	JDBCConnectionManager	modelfactory	= null;

	DataSource					source			= null;

	public SQLQueryValidator(DataSource source, Query q) {
		super(q);
		this.source = source;
		sourceQuery = q;
	}

	@Override
	public Object execute() {

		if (model != null) {
			return model;
		} else {
			if (validate()) {
				return model;
			} else {
				return null;
			}
		}
	}

	@Override
	public boolean validate() {

		try {
			modelfactory = JDBCConnectionManager.getJDBCConnectionManager();
			if(!modelfactory.isConnectionAlive(source.getSourceID())){
				modelfactory.createConnection(source);
			}
			if (model != null) {

				IncrementalResultSetTableModel rstm = model;
				rstm.close();
			}
			ResultSet set = modelfactory.executeQuery(source.getSourceID(), sourceQuery.toString(), source);
			model = new IncrementalResultSetTableModel(set);
			return true;

		} catch (NoDatasourceSelectedException e) {
			reason = e;
			return false;
		} catch (SQLException e) {
			reason = e;
			return false;
		} catch (ClassNotFoundException e) {
			reason = e;
			return false;
		} catch (Exception e) {
			reason = e;
			return false;
		}
	}

	public void cancelValidation() throws SQLException {
//		modelfactory.cancelCurrentStatement();
		model.close();
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

	public void dispose() {

		if (model != null)
			model.close();
	}

}
