package it.unibz.krdb.sql;

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

import java.io.Serializable;
import java.util.*;

public class DBMetadata implements Serializable {

	private static final long serialVersionUID = -806363154890865756L;

	private final Map<RelationID, TableDefinition> tables = new HashMap<>();
	private final Map<RelationID, ViewDefinition> views = new HashMap<>();

	private final String driverName;
	private final String driverVersion;
	private final String databaseProductName;
	private final QuotedIDFactory idfac;

	/**
	 * Constructs an initial metadata with some general information about the
	 * database, e.g., the driver name, the database engine name.
	 * 
	 */

	DBMetadata(String driverName, String driverVersion, String databaseProductName, QuotedIDFactory idfac) {
		this.driverName = driverName;
		this.driverVersion = driverVersion;
		this.databaseProductName = databaseProductName;
		this.idfac = idfac;
	}

	
	public TableDefinition createTable(RelationID id) {
		TableDefinition table = new TableDefinition(id);
		add(table, tables);
		return table;
	}

	/**
	 * THESE VIEWS ARE CREATED ONLY BY SQLQueryParser AS ABBREVIATIONS OF COMPLEX UNPARSABLE SUBQUERIES
	 * 
	 * @param id
	 * @param sql
	 * @return
	 */
	
	public ViewDefinition createView(RelationID id, String sql) {
		ViewDefinition table = new ViewDefinition(id, sql);
		add(table, views);
		return table;
	}
	
	/**
	 * Inserts a new data definition to this metadata object. 
	 * 
	 * @param td
	 *            The data definition. It can be a {@link TableDefinition} or a
	 *            {@link ViewDefinition} object.
	 */
	private <T extends RelationDefinition> void add(T td, Map<RelationID, T> schema) {
		schema.put(td.getID(), td);
		if (td.getID().hasSchema()) {
			RelationID noSchemaID = td.getID().getSchemalessID();
			if (!schema.containsKey(noSchemaID)) {
				schema.put(noSchemaID, td);
			}
			else {
				System.err.println("DUPLICATE TABLE NAMES, USE QUALIFIED NAMES:\n" + td + "\nAND\n" + schema.get(noSchemaID));
				schema.remove(noSchemaID);
			}
		}
	}

	
	/**
	 * Retrieves the data definition object based on its name. The
	 * <name>name</name> can be either a table name or a view name.
	 * 
	 * @param name
	 *            The string name.
	 */
	public TableDefinition getTable(RelationID name) {
		TableDefinition def = tables.get(name);
		if (def == null && name.hasSchema()) {
			def = tables.get(name.getSchemalessID());
		}
		return def;
	}

	public RelationDefinition getRelation(RelationID name) {
		RelationDefinition def = getTable(name);
		if (def == null) {
			def = views.get(name);
			if (def == null && name.hasSchema()) {
				def = views.get(name.getSchemalessID());
			}
		}
		return def;
	}
	
	/**
	 * Retrieves the tables list form the metadata.
	 */
	public Collection<TableDefinition> getTables() {
		return Collections.unmodifiableCollection(tables.values());
	}

	/**
	 * Retrieves the views list form the metadata.
	 */
	public Collection<ViewDefinition> getViews() {
		return Collections.unmodifiableCollection(views.values());
	}



	public String getDriverName() {
		return driverName;
	}

	public String getDriverVersion() {
		return driverVersion;
	}

	public String getDatabaseProductName() {
		return databaseProductName;
	}

	public QuotedIDFactory getQuotedIDFactory() {
		return idfac;
	}
	
	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		for (RelationID key : tables.keySet()) {
			bf.append(key);
			bf.append("=");
			bf.append(tables.get(key).toString());
			bf.append("\n");
		}
		for (RelationID key : views.keySet()) {
			bf.append(key);
			bf.append("=");
			bf.append(views.get(key).toString());
			bf.append("\n");
		}
		return bf.toString();
	}
}
