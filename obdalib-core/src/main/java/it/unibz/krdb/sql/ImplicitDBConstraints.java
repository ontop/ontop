/**
 * 
 */
package it.unibz.krdb.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 
 * Used for reading user-provided information about keys in views and
 * materialized views. Necessary for better performance in cases where
 * materialized views do a lot of work
 * 
 * 
 * Associated JUnit Tests @TestImplicitDBConstraints, @TestQuestImplicitDBConstraints
 * 
 * 
 *  @author Dag Hovland
 *
 */
public class ImplicitDBConstraints {
	
	private static Logger log = LoggerFactory.getLogger(ImplicitDBConstraints.class);
	
	private static final class Reference {
		final String fkTable, fkColumn;
		
		Reference(String fkTable, String fkColumn) {
			this.fkTable = fkTable;
			this.fkColumn = fkColumn;
		}

		String getColumnReference() {
			return fkColumn;
		}

		String getTableReference() {
			return fkTable;
		}	
	}
	
	// The key is a table name, each element in the array list is a primary key, 
	//            which is a list of the attributes making up the key
	private final Map<String, List<List<String>>> uniqueFD = new HashMap<>();
	
	// The key is a table name, and the values are all the foreign keys. 
	// The keys in the inner hash map are column names, while Reference object refers to a table
	private final Map<String, List<Map<String, Reference>>> fKeys = new HashMap<>();
	
	// Lists all tables referred to with a foreign key 
	//    Used to read metadata also from these 
	private final Set<String> referredTables = new HashSet<>();

	
	private final QuotedIDFactory idfac;
	
	
	/**
	 * Reads colon separated pairs of view name and primary key
	 * @param filename The name of the plain-text file with the fake keys
	 * @throws IOException 
	 */
	public ImplicitDBConstraints(String filename, QuotedIDFactory idfac) {
		this(new File(filename), idfac);
	}

	/**
	 * Reads colon-separated pairs of view name and primary key
	 * 
	 * @param file The plain-text file with functional dependencies
	 * 
	 * @throws IOException 
	 */
	public ImplicitDBConstraints(File file, QuotedIDFactory idfac) {
		this.idfac = idfac;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(":");
				if (parts.length >= 2){ // Primary or foreign key
					String tableName = parts[0];
					String[] keyColumns = parts[1].split(",");
					if (parts.length == 2) { // Primary Key		
						List<List<String>> pKeyList = uniqueFD.get(tableName);
						if (pKeyList == null){
							pKeyList = new ArrayList<>();
							uniqueFD.put(tableName, pKeyList);
						}
						List<String> pKey = new ArrayList<>();
						for(String pKeyCol : keyColumns){
							pKey.add(pKeyCol);
						}
						pKeyList.add(pKey);
					} 
					else if (parts.length == 4) { // Foreign Key
						String fkTable = parts[2];
						this.referredTables.add(fkTable);
						String[] fkColumnS = parts[3].split(",");			
						if (fkColumnS.length != keyColumns.length) {
							log.warn("Compound foreign key refers to different number of columns: " + line);
							continue;
						}
						
						List<Map<String, Reference>> tableFKeys = fKeys.get(tableName);
						if(tableFKeys == null){
							tableFKeys = new ArrayList<>();
							fKeys.put(tableName, tableFKeys);
						}
						Map<String, Reference> fKey = new HashMap<>();
						String fkName = keyColumns[0] + fkTable;
						for (int i = 0; i < fkColumnS.length; i++){
							String keyColumn = keyColumns[i];
							String fkColumn = fkColumnS[i];
							
							Reference ref = new Reference(fkTable, fkColumn);
							fKey.put(keyColumn, ref);
						}
						tableFKeys.add(fKey);
					}
				}
			}
		} 
		catch (FileNotFoundException e) {
			log.warn("Could not find file " + file + " in directory " + System.getenv().get("PWD"));
			String currentDir = System.getProperty("user.dir");
			log.warn("Current dir using System:" +currentDir);
			throw new IllegalArgumentException("File " + file + " does not exist");
		} 
		catch (IOException e) {
			log.warn("Problem reading keys from  file " + file);
			log.warn(e.getMessage());
		} 
	}

	/**
	 * Adds RelationJSQL for all tables referred to by the user supplied foreign keys
	 * 
	 * @param realTables The new table names are added to this list
	 * @return The parameter tables is returned, possible extended with new tables
	 */
	public void addReferredTables(Set<RelationID> realTables) {
		for (String tableGivenName : this.referredTables) {
			RelationID id = idfac.createRelationFromString(tableGivenName);
			realTables.add(id);
		}
	}

	
	/**
	 * Inserts the user-supplied primary keys / unique valued columns into the metadata object
	 */
	public void addFunctionalDependencies(DBMetadata md) {
		
		for (String tableName : this.uniqueFD.keySet()) {
			RelationID tableId = idfac.createRelationFromString(tableName);
			RelationDefinition td = md.getDefinition(tableId);
			if (td != null && td instanceof TableDefinition) {
				List<List<String>> tableFDs = this.uniqueFD.get(tableName);
				for (List<String> listOfConstraints: tableFDs) {
					for (String keyColumn : listOfConstraints) {
						QuotedID columnId = idfac.createFromString(keyColumn);
						Attribute attr = td.getAttribute(columnId);
						if (attr == null) {
							System.out.println("Column '" + keyColumn + "' not found in table '" + td.getID() + "'");
						} 
						else {		
							//td.setAttribute(key_pos, new Attribute(td, attr.getName(), attr.getType(), false, attr.getSQLTypeName())); // true
							// ROMAN (17 Aug 2015): do we really change it into NON NULL?
							td.addUniqueConstraint(UniqueConstraint.of(attr));
						}
					}
				}							
				md.add(td);
			} 
			else { // no table definition
				log.warn("Error in user supplied primary key: No table definition found for " + tableName + ".");
			}
		}		
	}



	/**
	 * Inserts the user-supplied foreign keys / unique valued columns into the metadata object
	 */
	public void addForeignKeys(DBMetadata md) {
		
		for (String tableName : this.fKeys.keySet()) {
			RelationID tableId = idfac.createRelationFromString(tableName);
			RelationDefinition td = md.getDefinition(tableId);
			if (td == null || ! (td instanceof TableDefinition)){
				log.warn("Error in user-supplied foreign key: Table '" + tableName + "' not found");
				continue;
			}
			List<Map<String, Reference>> tableFKeys = this.fKeys.get(tableName);
			for (Map<String, Reference> fKey : tableFKeys) {
				for (Map.Entry<String, Reference> entry : fKey.entrySet()) {
					QuotedID attrId = idfac.createFromString(entry.getKey());
					Attribute attr = td.getAttribute(attrId);
					if(attr == null){
						log.warn("Error getting attribute " + entry.getKey() + " from table " + tableName);
						continue;
					}
					String fkTable = entry.getValue().getTableReference();
					RelationID fkTableId = idfac.createRelationFromString(fkTable);
					RelationDefinition fktd = md.getDefinition(fkTableId);
					if (fktd == null) {
						log.warn("Error in user-supplied foreign key: Reference to non-existing table '" + fkTable + "'");
						continue;
					}
					String fkColumn = entry.getValue().getColumnReference();
					QuotedID fkAttrId = idfac.createFromString(fkColumn);
					Attribute fkAttr = fktd.getAttribute(fkAttrId);
					if (fkAttr == null) {
						log.warn("Error in user-supplied foreign key: Reference to non-existing column '" + fkColumn + "' in table '" + fkTable + "'");
						continue;
					}
					
					td.addForeignKeyConstraint(
							new ForeignKeyConstraint.Builder(td, fktd).add(attr, fkAttr)
									.build("_FK_" + tableName + "_" + entry.getKey()));
				}
			}
			md.add(td);
		}		
	}
}
