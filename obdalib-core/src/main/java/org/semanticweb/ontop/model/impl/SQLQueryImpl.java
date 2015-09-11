package org.semanticweb.ontop.model.impl;

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

import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.OBDAQueryModifiers;
import org.semanticweb.ontop.model.OBDASQLQuery;

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
		return new MutableQueryModifiersImpl();
	}

	@Override
	public boolean hasModifiers() {
		return false;
	}
}
