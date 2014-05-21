package org.semanticweb.ontop.sql;

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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.semanticweb.ontop.model.BooleanOperationPredicate;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.DatalogProgram;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.parser.SQLQueryTranslator;
import org.semanticweb.ontop.sql.api.Attribute;
import org.semanticweb.ontop.sql.api.VisitedQuery;

import net.sf.jsqlparser.JSQLParserException;
/**
 * This class keeps the meta information from the database, also keeps the 
 * view definitions, table definitions, attributes of DB tables, etc.
 * @author KRDB
 *
 */
public class DBMetadata implements Serializable {

	private static final long serialVersionUID = -806363154890865756L;

	private HashMap<String, DataDefinition> schema = new HashMap<String, DataDefinition>();

	private String driverName;
	private String databaseProductName;

	private boolean storesLowerCaseIdentifiers = false;
	private boolean storesLowerCaseQuotedIdentifiers = false;
	private boolean storesMixedCaseQuotedIdentifiers = false;
	private boolean storesMixedCaseIdentifiers = true;
	private boolean storesUpperCaseQuotedIdentifiers = false;
	private boolean storesUpperCaseIdentifiers = false;
	private static Pattern pQuotes = Pattern.compile("[\"`\\[][^\\.]*[\"`\\]]");;

	/**
	 * Constructs a blank metadata. Use only for testing purpose.
	 */
	public DBMetadata() {
		// NO-OP
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
		load(md);
	}

	/**
	 * Load some general information about the database metadata.
	 * 
	 * @param md
	 *            The database metadata.
	 */
	public void load(DatabaseMetaData md) {
		try {
			setDriverName(md.getDriverName());
			setDatabaseProductName(md.getDatabaseProductName());
			setStoresLowerCaseIdentifier(md.storesLowerCaseIdentifiers());
			setStoresLowerCaseQuotedIdentifiers(md
					.storesLowerCaseQuotedIdentifiers());
			setStoresMixedCaseIdentifiers(md.storesMixedCaseIdentifiers());
			setStoresMixedCaseQuotedIdentifiers(md
					.storesMixedCaseQuotedIdentifiers());
			setStoresUpperCaseIdentifiers(md.storesUpperCaseIdentifiers());
			setStoresUpperCaseQuotedIdentifiers(md
					.storesUpperCaseQuotedIdentifiers());
		} catch (SQLException e) {
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
	 * Inserts a list of data definition in batch.
	 * 
	 * @param list
	 *            A list of data definition.
	 */
	public void add(List<DataDefinition> list) {
		for (DataDefinition value : list) {
			add(value);
		}
	}

	/**
	 * Retrieves the data definition object based on its name. The
	 * <code>name</code> can be either a table name or a view name.
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
	public List<DataDefinition> getRelationList() {
		return new ArrayList<DataDefinition>(schema.values());
	}

	/**
	 * Retrieves the table list form the metadata.
	 */
	public List<TableDefinition> getTableList() {
		List<TableDefinition> tableList = new ArrayList<TableDefinition>();
		for (DataDefinition dd : getRelationList()) {
			if (dd instanceof TableDefinition) {
				tableList.add((TableDefinition) dd);
			}
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
	 * Returns the attribute position in the database metadata given the table
	 * name and the attribute name.
	 * 
	 * @param tableName
	 *            Can be a table name or a view name.
	 * @param attributeName
	 *            The target attribute name.
	 * @return Returns the index position or -1 if attribute name can't be found
	 */
	public int getAttributeIndex(String tableName, String attributeName) {
		DataDefinition dd = getDefinition(tableName);
		if (dd == null) {
			throw new RuntimeException("Unknown table definition: " + tableName);
		}
		return dd.getAttributePosition(attributeName);
	}

	/**
	 * Returns the attribute position in the database metadata given only the
	 * attribute name. The method will search to all tables in the schema and
	 * can throw ambiguous name exception if more than one table use the same
	 * name.
	 * 
	 * @param attributeName
	 *            The target attribute name.
	 * @return Returns the index position or -1 if attribute name can't be found
	 */
	public int getAttributeIndex(String attributeName) {
		int index = -1;
		for (String tableName : schema.keySet()) {
			int pos = getAttributeIndex(tableName, attributeName);
			if (pos != -1) {
				if (index == -1) {
					// If previously no table uses the attribute name.
					index = pos;
				} else {
					// Found a same name
					throw new RuntimeException(String.format(
							"The column name \"%s\" is ambiguous.",
							attributeName));
				}
			}
		}
		return index;
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

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}

	public String getDatabaseProductName() {
		return databaseProductName;
	}

	public void setStoresLowerCaseIdentifier(boolean storesLowerCaseIdentifiers) {
		this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
	}

	public boolean getStoresLowerCaseIdentifiers() {
		return storesLowerCaseIdentifiers;
	}

	public void setStoresLowerCaseQuotedIdentifiers(
			boolean storesLowerCaseQuotedIdentifiers) {
		this.storesLowerCaseQuotedIdentifiers = storesLowerCaseQuotedIdentifiers;
	}

	public boolean getStoresLowerCaseQuotedIdentifiers() {
		return storesLowerCaseQuotedIdentifiers;
	}

	public void setStoresMixedCaseQuotedIdentifiers(
			boolean storesMixedCaseQuotedIdentifiers) {
		this.storesMixedCaseQuotedIdentifiers = storesMixedCaseQuotedIdentifiers;
	}

	public boolean getStoresMixedCaseQuotedIdentifiers() {
		return storesMixedCaseQuotedIdentifiers;
	}

	public void setStoresMixedCaseIdentifiers(boolean storesMixedCaseIdentifiers) {
		this.storesMixedCaseIdentifiers = storesMixedCaseIdentifiers;
	}

	public boolean getStoresMixedCaseIdentifiers() {
		return storesMixedCaseIdentifiers;
	}

	public void setStoresUpperCaseQuotedIdentifiers(
			boolean storesUpperCaseQuotedIdentifiers) {
		this.storesUpperCaseQuotedIdentifiers = storesUpperCaseQuotedIdentifiers;
	}

	public boolean getStoresUpperCaseQuotedIdentifiers() {
		return storesUpperCaseQuotedIdentifiers;
	}

	public void setStoresUpperCaseIdentifiers(boolean storesUpperCaseIdentifiers) {
		this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;
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
	 * @param pkeys
	 * @param program
	 */
	public static Map<Predicate, List<Integer>> extractPKs(DBMetadata metadata,
			DatalogProgram program) {
		Map<Predicate, List<Integer>> pkeys = new HashMap<Predicate, List<Integer>>();
		for (CQIE mapping : program.getRules()) {
			for (Function newatom : mapping.getBody()) {
				Predicate newAtomPredicate = newatom.getFunctionSymbol();
				if (newAtomPredicate instanceof BooleanOperationPredicate) {
					continue;
				}
				// TODO Check this: somehow the new atom name is "Join" instead
				// of table name.
				String newAtomName = newAtomPredicate.toString();
				DataDefinition def = metadata.getDefinition(newAtomName);
				if (def != null) {
					List<Integer> pkeyIdx = new LinkedList<Integer>();
					for (int columnidx = 1; columnidx <= def.countAttribute(); columnidx++) {
						Attribute column = def.getAttribute(columnidx);
						if (column.isPrimaryKey()) {
							pkeyIdx.add(columnidx);
						}
					}
					if (!pkeyIdx.isEmpty()) {
						pkeys.put(newatom.getFunctionSymbol(), pkeyIdx);
					}
				}
			}
		}
		return pkeys;
	}
	
	/**
	 * Creates a view structure from an SQL string. See {@link #ViewDefinition}.
	 * @param name
	 * 			The name that the view will have. For instance "QEmployeeView"
	 * @param sqlString
	 * 			The SQL string defining the view
	 * @param isAnsPredicate
	 * 			It is true if the SQL comes from translating an ans predicate
	 * @return
	 */
	public ViewDefinition createViewDefinition(String name, String sqlString) {

		VisitedQuery visitedQuery = null;
		List<String> columns = null;
		SQLQueryTranslator translator = new SQLQueryTranslator();
		visitedQuery = translator.constructParserNoView(sqlString);
		
		columns = visitedQuery.getColumns();

		return createViewDefinition(name, sqlString, columns);
	}

	public ViewDefinition createViewDefinition(String name, String sqlString,
			Collection<String> columns) {

		

		ViewDefinition vd = new ViewDefinition(name);
		vd.setSQL(sqlString);
		int pos = 1;

		for (String column : columns) {
			vd.setAttribute(pos, new Attribute(column));
			pos++;
		}

		return vd;
	}

}
