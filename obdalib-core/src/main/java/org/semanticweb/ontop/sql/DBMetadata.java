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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.regex.Pattern;

import org.semanticweb.ontop.model.BooleanOperationPredicate;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.parser.SQLQueryParser;
import org.semanticweb.ontop.sql.api.Attribute;
import org.semanticweb.ontop.sql.api.ParsedSQLQuery;


/**
 * This class keeps the meta information from the database, also keeps the 
 * view definitions, table definitions, attributes of DB tables, etc.
 * @author KRDB
 *
 * TODO: see if we can make it immutable so that it can be shared between threads.
 *
 */
public class DBMetadata implements Serializable {

	private static final long serialVersionUID = -806363154890865756L;

	private final Map<String, DataDefinition> schema;

	private final String driverName;
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
        this.schema = new HashMap<>();
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
        this.schema = new HashMap<>();
		
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

    protected DBMetadata(Map<String, DataDefinition> schema,
                         String driverName, String databaseProductName, boolean storesLowerCaseIdentifiers,
                         boolean storesLowerCaseQuotedIdentifiers, boolean storesMixedCaseQuotedIdentifiers,
                         boolean storesMixedCaseIdentifiers, boolean storesUpperCaseQuotedIdentifiers,
                         boolean storesUpperCaseIdentifiers) {
        this.schema = new HashMap<>();

        /*
         * DataDefinition is mutable so should be cloned.
         */
        for (Map.Entry<String, DataDefinition> entry: schema.entrySet()) {
            this.schema.put(entry.getKey(), entry.getValue().cloneDefinition());
        }

        this.driverName = driverName;
        this.databaseProductName = databaseProductName;
        this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
        this.storesLowerCaseQuotedIdentifiers = storesLowerCaseQuotedIdentifiers;
        this.storesMixedCaseQuotedIdentifiers = storesMixedCaseQuotedIdentifiers;
        this.storesMixedCaseIdentifiers = storesMixedCaseIdentifiers;
        this.storesUpperCaseQuotedIdentifiers = storesUpperCaseQuotedIdentifiers;
        this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;
    }

    /**
     * DBMetadata is not immutable so it should not be shared between multiple threads
     *
     * @return
     */
    public DBMetadata clone() {
        return new DBMetadata(schema, driverName, databaseProductName, storesLowerCaseIdentifiers, storesLowerCaseQuotedIdentifiers,
                storesMixedCaseQuotedIdentifiers, storesMixedCaseIdentifiers, storesUpperCaseQuotedIdentifiers, storesUpperCaseIdentifiers);
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
	public Collection<DataDefinition> getRelations() {
		return Collections.unmodifiableCollection(schema.values());
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
	private String getAttributeName(String tableName, int pos) {
		DataDefinition dd = getDefinition(tableName);
		if (dd == null) 
			throw new RuntimeException("Unknown table definition: " + tableName);
		
		return dd.getAttribute(pos).getName();
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
     * It also returns the columns with unique constraints
     *
     * For instance, Given the table definition
     *   Tab0[col1:pk, col2:pk, col3, col4:unique, col5:unique],
     *
     * The methods will return the following Multimap:
     *  { Tab0 -> { [col1, col2], [col4], [col5] } }
     *
	 * 
	 * @param metadata
	 * @param program
	 */
	public static Multimap<Predicate, List<Integer>> extractPKs(DBMetadata metadata,
			List<CQIE> program) {
		Multimap<Predicate, List<Integer>> pkeys = HashMultimap.create();
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
                    // primary keys
					List<Integer> pkeyIdx = new LinkedList<>();
					for (int columnidx = 1; columnidx <= def.getAttributes().size(); columnidx++) {
						Attribute column = def.getAttribute(columnidx);
						if (column.isPrimaryKey())
							pkeyIdx.add(columnidx);
					}
					if (!pkeyIdx.isEmpty()) {
						pkeys.put(newAtomPredicate, pkeyIdx);
					}

                    // unique constraints
                    for (int columnidx = 1; columnidx <= def.getAttributes().size(); columnidx++) {
                        Attribute column = def.getAttribute(columnidx);
                        if (column.isUnique()) {
                            pkeys.put(newAtomPredicate, ImmutableList.of(columnidx));
                        }
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

        ParsedSQLQuery visitedQuery = null;
        List<String> columns = null;
        SQLQueryParser translator = new SQLQueryParser();
        //visitedQuery = translator.constructParserNoView(sqlString);
        visitedQuery = translator.parseShallowly(sqlString);

        columns = visitedQuery.getColumns();

        return createViewDefinition(name, sqlString, columns);
    }

    public ViewDefinition createViewDefinition(String name, String sqlString,
                                               Collection<String> columns) {



        ViewDefinition vd = new ViewDefinition(name, sqlString);
        int pos = 1;

        for (String column : columns) {
            vd.setAttribute(pos, new Attribute(column));
            pos++;
        }

        return vd;
    }

}
