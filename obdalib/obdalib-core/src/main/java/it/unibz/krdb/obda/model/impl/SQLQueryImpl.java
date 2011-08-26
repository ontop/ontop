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
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.OBDASQLQuery;

public class SQLQueryImpl implements OBDAQuery, OBDASQLQuery {

	private final String	sqlQuery;


	protected SQLQueryImpl(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	@Override
	public String toString() {
		if ((sqlQuery == null) || (sqlQuery.equals(""))) {
			return "";
		}
		return sqlQuery;
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.SQLQuery#clone()
	 */
	@Override
	public SQLQueryImpl clone() {
		SQLQueryImpl clone = new SQLQueryImpl(new String(sqlQuery));
		return clone;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	/* (non-Javadoc)
	 * @see inf.unibz.it.obda.model.impl.SQLQuery#getQueryModifiers()
	 */
	@Override
	public OBDAQueryModifiers getQueryModifiers() {
		return new OBDAQueryModifiers();
	}

	@Override
	public void setQueryModifiers(OBDAQueryModifiers modifiers) {
		
		
	}

}
