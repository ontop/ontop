package it.unibz.krdb.sql;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.VisitedQuery;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statement;
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
		DataDefinition def = schema.get(name);
		if (def == null)
			def = schema.get(name.toLowerCase());
		if (def == null)
			def = schema.get(name.toUpperCase());
		return def;
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
				Predicate newAtomPredicate = newatom.getFunctionSymbol();
				if (newAtomPredicate instanceof BooleanOperationPredicate) {
					continue;
				}
				// TODO Check this: somehow the new atom name is "Join" instead of table name.
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
	public ViewDefinition createViewDefinition(String name, String sqlString,
			boolean isAnsPredicate) {
		
		VisitedQuery queryP = null;
		List<String> columns;
		try {
			queryP = new VisitedQuery(sqlString);
			columns = queryP.getColumns();
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Statement statement = queryP.getStatement();
		
		HashMap<String,String> aliasMap = queryP.getAliasMap();
		
		
		 
			 

		
		
		ViewDefinition vd = new ViewDefinition(name);
		vd.setSQL(sqlString);
		int pos = 1;
		List<Attribute> listOfAttributes = new LinkedList<Attribute>();
		if (isAnsPredicate) {
			listOfAttributes = collectAttributesFromAnsView(sqlString);
		} else {
			listOfAttributes = collectAttributesFromQuery(sqlString);
		}

		for (Attribute attr : listOfAttributes) {
			vd.setAttribute(pos, attr);
			pos++;

		}
		return vd;
	}

	private List<Attribute> collectAttributesFromQuery(String sqlString) {
		List<Attribute> attributeList = new LinkedList<Attribute>();
		int start =  6; // the position of 'select' keyword 
		int end = sqlString.toLowerCase().indexOf("from");	// find the position of 'from' keyword	
		
		if (end == -1) {
			throw new RuntimeException("Error parsing SQL query: Couldn't find FROM keyword");
		}
		// The projection string will contain the column names separated by commas.
		//but it might have commas inside REPLACE statements, therefore we need to go
		//trough the string extracting the columns
		String projection = sqlString.substring(start, end).trim();
		char[] listchar = projection.toCharArray();
		List<String> columns = new LinkedList<String>(); //here we keep the column names
		String tempattr = new String();
		getColumnsFromString(listchar, columns, tempattr, false);
		
		
		
		/*
		 * Now we have to check every column and see if it has a proper format:
		 * table.name
		 * name
		 * "something" AS name
		 */
		Pattern pattern = Pattern.compile("^((\\w|_)+(\\.(\\w|_)+){0,2})");
		Pattern aliasPattern = Pattern.compile("[as]\\s+(.*)", Pattern.CASE_INSENSITIVE);
		
		for (int i = 0; i < columns.size(); i++) {
			String columnName = columns.get(i).trim();
			
			Matcher matcher = pattern.matcher(columnName);
			boolean patternFound = matcher.matches();
			if (!patternFound) {
				
				Matcher aliasMatch = aliasPattern.matcher(columnName);
				if (aliasMatch.find()) { // has an alias
					columnName = aliasMatch.group(1);  // make the alias name as the column name
				} else {
					throw new RuntimeException("Cannot parse the expression: " + columnName + " in SELECT statment. AS statement is probably required");
				}
			}			
			Attribute atname = new Attribute(columnName);
			attributeList.add(atname);
		}
		return attributeList;
	}

	
	
	/**
	 * This method takes the string generated from translated a Datalog rule defining an ans predicate into SQL.
	 * and extract the variables in the SELECT statement. 
	 * Observe that for each variable in ans the SQL translator generates 3 in SQL:
	 * <ul>
	 * <li> Quest Type</li>
	 * <li> Quest Language</li>
	 * <li> Value</li>
	 * </ul>
	 * In this last one is where the actual value from the database will be. Therefore it is the only one we keep.
	 * @param sqlString
	 * @return
	 */
	private List<Attribute> collectAttributesFromAnsView(String sqlString) {
		List<Attribute> attributeList = new LinkedList<Attribute>();
		System.err.println("DBMetadata: Optimize this collectAttribute");
		int start =  sqlString.toLowerCase().indexOf("select") + 6; // the position of 'select' keyword 
		int end = sqlString.toLowerCase().indexOf("from");	// find the position of 'from' keyword	
		
		if (end == -1) {
			throw new RuntimeException("Error parsing SQL query: Couldn't find FROM keyword");
		}
		// The projection string will contain the column names separated by commas.
		//but it might have commas inside REPLACE statements, therefore we need to go
		//trough the string extracting the columns
		String projection = sqlString.substring(start, end).trim();
		char[] listchar = projection.toCharArray();
		List<String> columns = new LinkedList<String>(); //here we keep the column names
		String tempattr = new String();
		
		getColumnsFromString(listchar, columns, tempattr, true);
		
		
		
		/*
		 * Now we have to check every column and see if it has a proper format:
		 * table.name
		 * name
		 * "something" AS name
		 */
		Pattern aliasPattern = Pattern.compile(".+[as]\\s+(.*)", Pattern.CASE_INSENSITIVE);
		
		for (int i = 0; i < columns.size(); i++) {
			String columnName = columns.get(i).trim();
			Matcher aliasMatch = aliasPattern.matcher(columnName);
			if (aliasMatch.find()) { // has an alias
					columnName = aliasMatch.group(1);  // make the alias name as the column name
					columnName = columnName.substring(1, columnName.length()-1);
				} else {
					throw new RuntimeException("Cannot parse the expression: " + columnName + " in SELECT statment. AS statement is probably required");
				}
			Attribute atname = new Attribute(columnName);
			attributeList.add(atname);
		}			
		return attributeList;
	}

	/*
	 * This method is a helper method for {@link #collectAttributesFromAnsView} and
	 * {@link #collectAttributesFromQuery}.
	 * It takes the string between the 'select' and 'from' statement of the SQL
	 * and return a clean list of string representing each column.
	 * 
	 */
	private void getColumnsFromString(char[] listchar, List<String> columns,
			String tempattr, boolean isAns) {
		boolean ignore = false;
		boolean isCast = false;
		int bracketbalance =0;
		int columnIndex = 0;

		for (int i = 0, n = listchar.length; i < n; i++){
			char symbol=listchar[i];
			if (symbol!=','&& ( symbol!='(' || isCast ) && !ignore){
				tempattr= tempattr + symbol;
			} else if (symbol=='(' && ignore == false&& isCast == false) {
				boolean castString = tempattr.trim().equals("CAST");
				if (!castString){
					ignore= true;
					bracketbalance =1; 
				} else {
					isCast = true;
				}
				tempattr= tempattr + symbol;
			} else if (ignore == true && bracketbalance>0) {
				tempattr= tempattr + symbol;
				if (symbol=='('){
					bracketbalance++;
				}
				if (symbol==')'){
					bracketbalance--;
				}
				
			} else if  (ignore == true && bracketbalance==0){
				ignore =  false;
			} else if (symbol==',' && bracketbalance==0 && ignore == false){
				columnIndex ++;
				if (isAns && columnIndex % 3 == 0){
					columns.add(removeQuotes(tempattr));
				} else if (! isAns){
					columns.add(removeQuotes(tempattr));
				}
				isCast = false;
				tempattr = "";
			} 
			
		}
		columns.add(removeQuotes(tempattr));

	}
	
	private String removeQuotes(String str) {
		return str.replace("\"", "");
	}

}
