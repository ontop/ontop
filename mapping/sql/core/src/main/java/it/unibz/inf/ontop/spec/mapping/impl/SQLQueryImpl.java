package it.unibz.inf.ontop.spec.mapping.impl;

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

import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;

public class SQLQueryImpl implements OBDASQLQuery {

	private final String sqlQuery;

	protected SQLQueryImpl(String sqlQuery) {
		this.sqlQuery = sqlQuery.replaceAll("(?m)^[ \t]*\r?\n", "");
	}

	@Override
	public String getSQLQuery() { return sqlQuery; }
	
	@Override
	public String toString() {
		return getSQLQuery();
	}

	@Override
	public SQLQueryImpl clone() {
		throw new IllegalArgumentException();
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}
