package it.unibz.krdb.sql;

import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.sql.api.Attribute;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
			setStoresLowerCaseQuotedIdentifiers(md.storesLowerCaseQuotedIdentifiers());
			setStoresMixedCaseIdentifiers(md.storesMixedCaseIdentifiers());
			setStoresMixedCaseQuotedIdentifiers(md.storesMixedCaseQuotedIdentifiers());
			setStoresUpperCaseIdentifiers(md.storesUpperCaseIdentifiers());
			setStoresUpperCaseQuotedIdentifiers(md.storesUpperCaseQuotedIdentifiers());
		} catch (SQLException e) {
			throw new RuntimeException("Failed on importing database metadata!\n" + e.getMessage());
		}
	}

	/**
	 * Inserts a new data definition to this meta data object.
	 * 
	 * @param value
	 *            The data definition. It can be a {@link TableDefinition} or a
	 *            {@link ViewDefinition} object.
	 */
	public void add(DataDefinition value) {
		schema.put(value.getName(), value);
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
	 * <name>name</name> can be either a table name or a view name.
	 * 
	 * @param name
	 *            The string name.
	 */
	public DataDefinition getDefinition(String name) {
		return schema.get(name);
	}
	
	/**
	 * Retrieves the relation list (table and view definition) form the metadata.
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
	 * Returns the attribute position in the database metadata given the table name
	 * and the attribute name.
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
	 * Returns the attribute position in the database metadata given only the attribute
	 * name. The method will search to all tables in the schema and can throw ambiguous
	 * name exception if more than one table use the same name.
	 * 
	 * @param attributeName
	 * 			The target attribute name.
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
					throw new RuntimeException(String.format("The column name \"%s\" is ambiguous.", attributeName));
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
		String value = String.format("%s.%s", name, getAttributeName(name, pos));
		return value;
	}
	
	/**
	 * Returns the attribute full-qualified name using the table/view ALIAS name.
	 * [ALIAS_NAME].[ATTRIBUTE_NAME]. If the alias name is blank, the method will
	 * use the table/view name: [TABLE_NAME].[ATTRIBUTE_NAME]. 
	 * 
	 * @param name
	 *            Can be a table name or a view name.
	 * @param alias
	 * 			  The table or view alias name.
	 * @param pos
	 *            The index position.
	 * @return
	 */
	public String getFullQualifiedAttributeName(String name, String alias, int pos) {
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

	public void setStoresLowerCaseQuotedIdentifiers(boolean storesLowerCaseQuotedIdentifiers) {
		this.storesLowerCaseQuotedIdentifiers = storesLowerCaseQuotedIdentifiers;
	}

	public boolean getStoresLowerCaseQuotedIdentifiers() {
		return storesLowerCaseQuotedIdentifiers;
	}

	public void setStoresMixedCaseQuotedIdentifiers(boolean storesMixedCaseQuotedIdentifiers) {
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

	public void setStoresUpperCaseQuotedIdentifiers(boolean storesUpperCaseQuotedIdentifiers) {
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
		StringBuffer bf = new StringBuffer();
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
	 * that contains the Primary Key data for the predicates obtained from the info in 
	 * the metadata.
	 * 
	 * @param metadata
	 * @param pkeys
	 * @param program
	 */
	public static Map<Predicate, List<Integer>> extractPKs(DBMetadata metadata, DatalogProgram program) {
		Map<Predicate, List<Integer>> pkeys = new HashMap<Predicate, List<Integer>>();

		for (CQIE mapping : program.getRules()) {
			for (Function newatom : mapping.getBody()) {
				Predicate newAtomPredicate = newatom.getPredicate();
				if (newAtomPredicate instanceof BooleanOperationPredicate) {
					continue;
				}
				String newAtomName = newAtomPredicate.toString();
				DataDefinition def = metadata.getDefinition(newAtomName);
				List<Integer> pkeyIdx = new LinkedList<Integer>();
				for (int columnidx = 1; columnidx <= def.countAttribute(); columnidx++) {
					Attribute column = def.getAttribute(columnidx);
					if (column.isPrimaryKey()) {
						pkeyIdx.add(columnidx);
					}
	
				}
				if (!pkeyIdx.isEmpty()) {
					pkeys.put(newatom.getPredicate(), pkeyIdx);
				}
	
			}
		}
		return pkeys;
	}
}
