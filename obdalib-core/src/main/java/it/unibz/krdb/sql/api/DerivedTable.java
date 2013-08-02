/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

/**
 * The DerivedTable class represents the notation of nested query
 * in SQL.
 */
public class DerivedTable implements ITable {
	
	private static final long serialVersionUID = -8310536644014311978L;

	private QueryTree subquery;

	private String alias;

	public DerivedTable(QueryTree subquery) {
		setSubQuery(subquery);
	}
	
	public void setSubQuery(QueryTree subquery) {
		this.subquery = subquery;
	}
	
	public QueryTree getSubQuery() {
		return subquery;
	}

	@Override
	public String toString() {
		return alias;
	}
}
