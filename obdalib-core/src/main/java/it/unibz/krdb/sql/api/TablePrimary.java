/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql.api;

public class TablePrimary implements ITable {
	
	private static final long serialVersionUID = -205626125381808960L;
	
	private String schema;
	private String name;
	private String alias;

	public TablePrimary(String name) {
		this("", name);
	}
	
	public TablePrimary(String schema, String name) {
		setSchema(schema);
		setName(name);
		setAlias("");
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setAlias(String alias) {
		if (alias == null) {
			return;
		}
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	@Override
	public String toString() {
		String str = "";
		if (schema != "") {
			str += schema + ".";
		}			
		str += name;
		if (alias != "") {
			str += " as " + alias;
		}
		return str;
	}
}
