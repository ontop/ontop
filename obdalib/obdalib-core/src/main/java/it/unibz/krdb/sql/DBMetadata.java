package it.unibz.krdb.sql;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

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
	 * Returns the attribute name based on the table/view name and its position
	 * in the meta data.
	 * 
	 * @param name
	 *            Can be a table name or a view name.
	 * @param pos
	 *            The index position.
	 * @return
	 */
	public String getAttributeName(String name, int pos) {
		DataDefinition dd = getDefinition(name);
		if (dd == null) {
			throw new RuntimeException("Unknown table definition: " + name);
		}
		return dd.getAttributeName(pos);
	}

	/**
	 * Returns the attribute full-qualified name based on the table/view name
	 * and its position in the meta data.
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
}
