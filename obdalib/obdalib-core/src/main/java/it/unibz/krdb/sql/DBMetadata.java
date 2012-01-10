package it.unibz.krdb.sql;

import java.util.HashMap;
import java.util.List;

public class DBMetadata {



	private HashMap<String, DataDefinition> schema = new HashMap<String, DataDefinition>();
	private boolean storesLowerCaseIdentifiers = false;
	private boolean storesLowerCaseQuotedIdentifiers = false;
	private boolean storesMixedCaseQuotedIdentifiers = false;
	private boolean storesMixedCaseIdentifiers = true;
	private boolean storesUpperCaseQuotedIdentifiers = false;
	private boolean storesUpperCaseIdentifiers = false;
	private String driverName;
	private String databaseProductName;

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

	public String getFormattedIdentifier(String identifier) {
		String result = identifier;
		if (storesMixedCaseIdentifiers) {
			// do nothing
		} else if (storesMixedCaseQuotedIdentifiers && result.charAt(0) != '\'') {
			result = "'" + result + "'";
		} else if (storesLowerCaseIdentifiers) {
			result = result.toLowerCase();
		} else if (storesLowerCaseQuotedIdentifiers && result.charAt(0) != '\'') {
			result = "'" + result.toLowerCase() + "'";
		} else if (storesUpperCaseIdentifiers) {
			result = result.toUpperCase();
		} else if (storesUpperCaseQuotedIdentifiers && result.charAt(0) != '\'') {
			result = "'" + result.toUpperCase() + "'";
		}
		return result;
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
	 * Retrieves the data definition object based on its name. The name can be
	 * either a table name or a view name.
	 * 
	 * @param name
	 *            The string name.
	 */
	public DataDefinition getDefinition(String name) {
		name = getFormattedIdentifier(name);

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

	public void setStoresLowerCaseIdentifier(boolean storesLowerCaseIdentifiers) {
		this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;

	}

	public void setStoresLowerCaseQuotedIdentifiers(boolean storesLowerCaseQuotedIdentifiers) {
		this.storesLowerCaseQuotedIdentifiers = storesLowerCaseQuotedIdentifiers;
	}

	public void setStoresMixedCaseQuotedIdentifiers(boolean storesMixedCaseQuotedIdentifiers) {
		if (databaseProductName.equals("H2")) {
			this.storesMixedCaseQuotedIdentifiers = false;
		} else {
			this.storesMixedCaseQuotedIdentifiers = storesMixedCaseQuotedIdentifiers;
		}

	}

	public void setStoresMixedCaseIdentifiers(boolean storesMixedCaseIdentifiers) {

		if (databaseProductName.equals("H2")) {
			this.storesMixedCaseIdentifiers = false;
		} else {
			this.storesMixedCaseIdentifiers = storesMixedCaseIdentifiers;
		}

	}

	public void setStoresUpperCaseQuotedIdentifiers(boolean storesUpperCaseQuotedIdentifiers) {
		this.storesUpperCaseQuotedIdentifiers = storesUpperCaseQuotedIdentifiers;

	}

	public void setStoresUpperCaseIdentifiers(boolean storesUpperCaseIdentifiers) {
		this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;

	}

	public boolean storesLowerCaseIdentifiers() {
		return this.storesLowerCaseIdentifiers;

	}

	public boolean storesLowerCaseQuotedIdentifiers() {
		return this.storesLowerCaseQuotedIdentifiers;
	}

	public boolean storesMixedCaseQuotedIdentifiers() {
		return this.storesMixedCaseQuotedIdentifiers;

	}

	public boolean storesMixedCaseIdentifiers() {
		return this.storesMixedCaseIdentifiers;

	}

	public boolean storesUpperCaseQuotedIdentifiers() {
		return this.storesUpperCaseQuotedIdentifiers;

	}

	public boolean storesUpperCaseIdentifiers() {
		return this.storesUpperCaseIdentifiers;

	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}

	public String getDriverName() {
		return driverName;
	}

	public String getDatabaseProductName() {
		return databaseProductName;
	}

}
