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

	private final Map<RelationID, DatabaseRelationDefinition> tables = new HashMap<>();
	
	// relations include tables and views (views are only created for complex queries in mappings)
	private final Map<RelationID, RelationDefinition> relations = new HashMap<>();
	private final List<DatabaseRelationDefinition> listOfTables = new LinkedList<>();

	private final String driverName;
	private final String driverVersion;
	private final String databaseProductName;
	private final String databaseVersion;
	private final QuotedIDFactory idfac;

	/**
	 * Constructs an initial metadata with some general information about the
	 * database, e.g., the driver name, the database engine name.
	 *
	 * DO NOT USE THIS CONSTRUCTOR -- USE MetadataExtractor METHODS INSTEAD
	 */

	DBMetadata(String driverName, String driverVersion, String databaseProductName, String databaseVersion, QuotedIDFactory idfac) {
		this.driverName = driverName;
		this.driverVersion = driverVersion;
		this.databaseProductName = databaseProductName;
		this.databaseVersion = databaseVersion;
		this.idfac = idfac;
	}

	/**
	 * creates a database table (which can also be a database view) 
	 * if the <name>id</name> contains schema than the relation is added 
	 * to the lookup table (see getDatabaseRelation and getRelation) with 
	 * both the fully qualified id and the table name only id
	 * 
	 * @param id
	 * @return
	 */
	
	public DatabaseRelationDefinition createDatabaseRelation(RelationID id) {
		DatabaseRelationDefinition table = new DatabaseRelationDefinition(id);
		add(table, tables);
		add(table, relations);
		listOfTables.add(table);
		return table;
	}

	
	private int parserViewCounter;
	
	/**
	 * creates a view for SQLQueryParser
	 * (NOTE: these views are simply names for complex non-parsable subqueries, not database views)
	 * 
	 * @param id
	 * @param sql
	 * @return
	 */
	
	public ParserViewDefinition createParserView(String sql) {
		RelationID id = idfac.createRelationID(null, String.format("view_%s", parserViewCounter++));	
		
		ParserViewDefinition view = new ParserViewDefinition(id, sql);
		add(view, relations);
		return view;
	}
	
	/**
	 * Inserts a new data definition to this metadata object. 
	 * 
	 * @param td
	 *            The data definition. It can be a {@link DatabaseRelationDefinition} or a
	 *            {@link ParserViewDefinition} object.
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
				//schema.remove(noSchemaID);
				// TODO (ROMAN 8 Oct 2015): think of a better way of resolving ambiguities 
			}
		}
	}

	
	/**
	 * Retrieves the data definition object based on its name. The
	 * <name>id</name> is a table name.
	 * If <name>id</name> has schema and the fully qualified id 
	 * cannot be resolved the the table-only id is used  
	 * 
	 * @param name
	 */
	public DatabaseRelationDefinition getDatabaseRelation(RelationID id) {
		DatabaseRelationDefinition def = tables.get(id);
		if (def == null && id.hasSchema()) {
			def = tables.get(id.getSchemalessID());
		}
		return def;
	}

	/**
	 * Retrieves the data definition object based on its name. The
	 * <name>name</name> can be either a table name or a view name.
	 * If <name>id</name> has schema and the fully qualified id 
	 * cannot be resolved the the table-only id is used  
	 * 
	 * @param name
	 */
	public RelationDefinition getRelation(RelationID name) {
		RelationDefinition def = relations.get(name);
		if (def == null && name.hasSchema()) {
			def = relations.get(name.getSchemalessID());
		}
		return def;
	}
	
	/**
	 * Retrieves the tables list form the metadata.
	 */
	public Collection<DatabaseRelationDefinition> getDatabaseRelations() {
		return Collections.unmodifiableCollection(listOfTables);
	}


	public String getDriverName() {
		return driverName;
	}

	public String getDriverVersion() {
		return driverVersion;
	}

	public String getDbmsProductName() {
		return databaseProductName;
	}
	
	public String getDbmsVersion() {
		return databaseVersion;
	}
	

	public QuotedIDFactory getQuotedIDFactory() {
		return idfac;
	}
	
	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		for (RelationID key : relations.keySet()) {
			bf.append(key);
			bf.append("=");
			bf.append(relations.get(key).toString());
			bf.append("\n");
		}
		return bf.toString();
	}
}
