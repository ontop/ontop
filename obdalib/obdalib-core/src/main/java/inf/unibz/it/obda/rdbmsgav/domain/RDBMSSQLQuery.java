/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and,
 * b) third-party components licensed under terms that may be different from
 *   those of the LGPL.  Information about such licenses can be found in the
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.rdbmsgav.domain;

import org.obda.query.domain.Query;



public class RDBMSSQLQuery implements Query {

	private final String sqlQuery;

	public RDBMSSQLQuery() {
		this.sqlQuery = null;
	}

	public RDBMSSQLQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	@Override
	public String toString() {
		if ((sqlQuery == null) || (sqlQuery.equals(""))) {
			return "";
		}
		return sqlQuery;
	}

	@Override
	public RDBMSSQLQuery clone() {
		RDBMSSQLQuery clone = new RDBMSSQLQuery(new String(sqlQuery));
		return clone;
	}

	@Override
	public int hashCode(){
		return this.toString().hashCode();
	}
}
