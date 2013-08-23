/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.OBDASQLQuery;

public class SQLQueryImpl implements OBDAQuery, OBDASQLQuery {

	private static final long serialVersionUID = -1910293716786132196L;
	
	private final String sqlQuery;

	protected SQLQueryImpl(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public String getSQLQuery() {
		if ((sqlQuery == null) || (sqlQuery.equals(""))) {
			return "";
		}
		return sqlQuery;
	}
	
	@Override
	public String toString() {
		return getSQLQuery();
	}

	@Override
	public SQLQueryImpl clone() {
		SQLQueryImpl clone = new SQLQueryImpl(new String(sqlQuery));
		return clone;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public OBDAQueryModifiers getQueryModifiers() {
		return new OBDAQueryModifiers();
	}

	@Override
	public void setQueryModifiers(OBDAQueryModifiers modifiers) {
		// NO-OP
	}
}
