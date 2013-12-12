/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

import net.sf.jsqlparser.schema.Table;

/**
 * The Relation class is a wrapper class that make the
 * {@link Table} class compatible with the 
 * abstraction in the {@link VisitedQuery}.
 */
public class RelationJSQL extends RelationalAlgebra {

	private static final long serialVersionUID = 8464933976477745339L;
	
	private TableJSQL table;
	
	public RelationJSQL(TableJSQL table) {
		this.table = table;
	}
	
	public String getSchema() {
		return table.getSchema();
	}
	
	public String getName() {
		return table.getName();
	}
	
	public String getTableName() {
		return table.getTableName();
	}
	
	public String getGivenName() {
		return table.getGivenName();
	}
	public String getAlias() {
		return table.getAlias();
	}
	
	@Override
	public String toString() {
		return table.toString();
	}
	
	@Override
	public RelationJSQL clone() {
		return new RelationJSQL(table);
	}
	
	@Override
	public boolean equals(Object r){
		if(r instanceof RelationJSQL)
			return this.table.getTableName().equals(((RelationJSQL)r).table.getTableName());
		return false;
	}
}
