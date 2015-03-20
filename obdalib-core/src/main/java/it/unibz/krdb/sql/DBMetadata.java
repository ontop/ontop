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

import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.sql.api.Attribute;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DBMetadata implements Serializable {

	private static final long serialVersionUID = -806363154890865756L;

	private final Map<String, DataDefinition> schema = new HashMap<>();

	private String driverName;
	private String driverVersion;
	private String databaseProductName;

	private boolean storesLowerCaseIdentifiers = false;
	private boolean storesLowerCaseQuotedIdentifiers = false;
	private boolean storesMixedCaseQuotedIdentifiers = false;
	private boolean storesMixedCaseIdentifiers = true;
	private boolean storesUpperCaseQuotedIdentifiers = false;
	private boolean storesUpperCaseIdentifiers = false;
	
	private static final Pattern pQuotes = Pattern.compile("[\"`\\[][^\\.]*[\"`\\]]");;

	/**
	 * Constructs a blank metadata. Use only for testing purpose.
	 */
	public DBMetadata(String driverName) {
		this.driverName = driverName;
	}

	/**
	 * Constructs an initial metadata with some general information about the
	 * database, e.g., the driver name, the database name and several rules on
	 * storing the identifier.
	 * 
	 * @param md
	 *            The database metadata.
	 */
	public DBMetadata(DatabaseMetaData md) {
		
		try {
			driverName = md.getDriverName();
			driverVersion = md.getDriverVersion();
			databaseProductName = md.getDatabaseProductName();
			
			storesLowerCaseIdentifiers = md.storesLowerCaseIdentifiers();
			storesLowerCaseQuotedIdentifiers = md.storesLowerCaseQuotedIdentifiers();

			storesMixedCaseIdentifiers = md.storesMixedCaseIdentifiers();
			storesMixedCaseQuotedIdentifiers = md.storesMixedCaseQuotedIdentifiers();
			
			storesUpperCaseIdentifiers = md.storesUpperCaseIdentifiers();
			storesUpperCaseQuotedIdentifiers = md.storesUpperCaseQuotedIdentifiers();
		} 
		catch (SQLException e) {
			throw new RuntimeException(
					"Failed on importing database metadata!\n" + e.getMessage());
		}
	}

	/**
	 * Inserts a new data definition to this meta data object. The name is
	 * inserted without quotes so it can be used for the mapping, while the
	 * value that is used also for the generated SQL conserves the quotes
	 * 
	 * @param value
	 *            The data definition. It can be a {@link TableDefinition} or a
	 *            {@link ViewDefinition} object.
	 */
	public void add(DataDefinition value) {
		String name = value.getName();
		// name without quotes
		if (pQuotes.matcher(name).matches())
			schema.put(name.substring(1, name.length() - 1), value);

		else {
			String[] names = name.split("\\."); // consider the case of
												// schema.table
			if (names.length == 2) {
				String schemaName = names[0];
				String tableName = names[1];
				if (pQuotes.matcher(schemaName).matches())
					schemaName = schemaName.substring(1,
							schemaName.length() - 1);
				if (pQuotes.matcher(tableName).matches())
					tableName = tableName.substring(1, tableName.length() - 1);
				schema.put(schemaName + "." + tableName, value);
			} else
				schema.put(name, value);
		}

	}

	/**
	 * Retrieves the data definition object based on its name. The
	 * <name>name</name> can be either a table name or a view name.
	 * 
	 * @param name
	 *            The string name.
	 */
	public DataDefinition getDefinition(String name) {
		DataDefinition def = schema.get(name);
		if (def == null)
			def = schema.get(name.toLowerCase());
		if (def == null)
			def = schema.get(name.toUpperCase());
		// if (def == null)
		// def = schema.get(name.substring(1, name.length()-1));
		return def;
	}

	/**
	 * Retrieves the relation list (table and view definition) form the
	 * metadata.
	 */
	public Collection<DataDefinition> getRelations() {
		return schema.values();
	}

	/**
	 * Retrieves the table list form the metadata.
	 */
	public Collection<TableDefinition> getTables() {
		List<TableDefinition> tableList = new ArrayList<>();
		for (DataDefinition dd : getRelations()) {
			if (dd instanceof TableDefinition) 
				tableList.add((TableDefinition) dd);
		}
		return tableList;
	}

	/**
	 * Returns the attribute name based on the table/view name and its position
	 * in the meta data.
	 * 
	 * @param tableName
	 *            Can be a table name or a view name.
	 * @param pos
	 *            The index position.
	 * @return
	 */
	public String getAttributeName(String tableName, int pos) {
		DataDefinition dd = getDefinition(tableName);
		if (dd == null) {
			throw new RuntimeException("Unknown table definition: " + tableName);
		}
		return dd.getAttributeName(pos);
	}


	/**
	 * Returns the attribute full-qualified name using the table/view name:
	 * [TABLE_NAME].[ATTRIBUTE_NAME]
	 * 
	 * @param name
	 *            Can be a table name or a view name.
	 * @param pos
	 *            The index position.
	 * @return
	 */
	public String getFullQualifiedAttributeName(String name, int pos) {

		String value = String
				.format("%s.%s", name, getAttributeName(name, pos));
		return value;
	}

	/**
	 * Returns the attribute full-qualified name using the table/view ALIAS
	 * name. [ALIAS_NAME].[ATTRIBUTE_NAME]. If the alias name is blank, the
	 * method will use the table/view name: [TABLE_NAME].[ATTRIBUTE_NAME].
	 * 
	 * @param name
	 *            Can be a table name or a view name.
	 * @param alias
	 *            The table or view alias name.
	 * @param pos
	 *            The index position.
	 * @return
	 */
	public String getFullQualifiedAttributeName(String name, String alias,
			int pos) {
		if (alias != null && !alias.isEmpty()) {
			return String.format("%s.%s", alias, getAttributeName(name, pos));
		} else {
			return getFullQualifiedAttributeName(name, pos);
		}
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

	public boolean getStoresLowerCaseIdentifiers() {
		return storesLowerCaseIdentifiers;
	}

	public boolean getStoresLowerCaseQuotedIdentifiers() {
		return storesLowerCaseQuotedIdentifiers;
	}

	public boolean getStoresMixedCaseQuotedIdentifiers() {
		return storesMixedCaseQuotedIdentifiers;
	}

	public boolean getStoresMixedCaseIdentifiers() {
		return storesMixedCaseIdentifiers;
	}

	public boolean getStoresUpperCaseQuotedIdentifiers() {
		return storesUpperCaseQuotedIdentifiers;
	}

	public boolean getStoresUpperCaseIdentifiers() {
		return storesUpperCaseIdentifiers;
	}

	@Override
	public String toString() {
		StringBuilder bf = new StringBuilder();
		for (String key : schema.keySet()) {
			bf.append(key);
			bf.append("=");
			bf.append(schema.get(key).toString());
			bf.append("\n");
		}
		return bf.toString();
	}

	/***
	 * Generates a map for each predicate in the body of the rules in 'program'
	 * that contains the Primary Key data for the predicates obtained from the
	 * info in the metadata.
	 * 
	 * @param metadata
	 * @param program
	 */
	public static Map<Predicate, List<Integer>> extractPKs(DBMetadata metadata,
			List<CQIE> program) {
		Map<Predicate, List<Integer>> pkeys = new HashMap<>();
		for (CQIE mapping : program) {
			for (Function newatom : mapping.getBody()) {
				Predicate newAtomPredicate = newatom.getFunctionSymbol();
				if (newAtomPredicate instanceof BooleanOperationPredicate) 
					continue;
				
				if (pkeys.containsKey(newAtomPredicate))
					continue;
				
				// TODO Check this: somehow the new atom name is "Join" instead
				// of table name.
				String newAtomName = newAtomPredicate.toString();
				DataDefinition def = metadata.getDefinition(newAtomName);
				if (def != null) {
					List<Integer> pkeyIdx = new LinkedList<>();
					for (int columnidx = 1; columnidx <= def.getNumOfAttributes(); columnidx++) {
						Attribute column = def.getAttribute(columnidx);
						if (column.isPrimaryKey()) 
							pkeyIdx.add(columnidx);
					}
					if (!pkeyIdx.isEmpty()) 
						pkeys.put(newAtomPredicate, pkeyIdx);
				}
			}
		}
		return pkeys;
	}


}
