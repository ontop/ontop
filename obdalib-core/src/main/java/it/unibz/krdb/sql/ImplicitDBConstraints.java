/**
 * 
 */
package it.unibz.krdb.sql;

import it.unibz.krdb.sql.api.RelationJSQL;
import it.unibz.krdb.sql.api.TableJSQL;
import net.sf.jsqlparser.schema.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
		final String fkName, fkTable, fkColumn;
		
		Reference(String fkName, String fkTable, String fkColumn) {
			this.fkName = fkName;
			this.fkTable = fkTable;
			this.fkColumn = fkColumn;
		}

		public String getColumnReference() {
			return fkColumn;
		}

		public String getTableReference() {
			return fkTable;
		}
		
	}
	
	// The key is a table name, each element in the array list is a primary key, which is a list of the keys making up the key
	HashMap<String, ArrayList<ArrayList<String>>> uniqueFD;
	// The key is a table name, and the values are all the foreign keys. The keys in the inner hash map are column names, while Reference object refers to a tabel
	HashMap<String, ArrayList<HashMap<String, Reference>>> fKeys;
	// Lists all tables referred to with a foreign key. Used to read metadata also from these 
	HashSet<String> referredTables;
	
	// File with the user-supplied constraints
	File file;
	
	/**
	 * Reads colon separated pairs of view name and primary key
	 * @param filename The name of the plain-text file with the fake keys
	 * @throws IOException 
	 */
	public ImplicitDBConstraints(String filename) {
		this(new File(filename));
	}

	/**
	 * Reads colon separated pairs of view name and primary key
	 * @param file The plain-text file with functional dependencies
	 * @throws IOException 
	 */
	public ImplicitDBConstraints(File file) {
		if(!file.exists()){
			throw new IllegalArgumentException("File " + file + " does not exist");
		}
		this.file = file;
		this.uniqueFD = new HashMap<>();
		this.fKeys = new HashMap<>();
		this.referredTables = new HashSet<>();
		this.parseConstraints();
	}

	private final void parseConstraints() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(":");
				if (parts.length >= 2){ // Primary or foreign key
					String tableName = parts[0];
					String[] keyColumns = parts[1].split(",");
					if(parts.length == 2) { // Primary key		
						ArrayList<ArrayList<String>> pKeyList = uniqueFD.get(tableName);
						if (pKeyList == null){
							pKeyList = new ArrayList<ArrayList<String>>();
							uniqueFD.put(tableName, pKeyList);
						}
						ArrayList<String> pKey = new ArrayList<String>();
						for(String pKeyCol : keyColumns){
							pKey.add(pKeyCol);
						}
						pKeyList.add(pKey);
					} else if (parts.length == 4){ // FOreign key
						String fkTable = parts[2];
						this.referredTables.add(fkTable);
						String[] fkColumnS = parts[3].split(",");
						
						if(fkColumnS.length != keyColumns.length){
							log.warn("Compound foreign key refers to different number of columns: " + line);
							continue;
						}
						
						ArrayList<HashMap<String, Reference>> tableFKeys = fKeys.get(tableName);
						if(tableFKeys == null){
							tableFKeys = new ArrayList<HashMap<String, Reference>>();
							fKeys.put(tableName, tableFKeys);
						}
						HashMap<String, Reference> fKey = new HashMap<String, Reference>();
						String fkName = keyColumns[0] + fkTable;
						for(int i = 0; i < fkColumnS.length; i++){
							String keyColumn = keyColumns[i];
							String fkColumn = fkColumnS[i];
							
							Reference ref = new Reference(fkName, fkTable, fkColumn);
							fKey.put(keyColumn, ref);
						}
						tableFKeys.add(fKey);
					}
				}
			}

		} catch (FileNotFoundException e) {
			log.warn("Could not find file " + file + " in directory " + System.getenv().get("PWD"));
			String currentDir = System.getProperty("user.dir");
			log.warn("Current dir using System:" +currentDir);
		} catch (IOException e) {
			log.warn("Problem reading keys from  file " + file);
			log.warn(e.getMessage());
		} 
		
	}
	
	/**
	 * Used by addReferredTables to check whether a RelationJSQL for the table "given name" 
	 * already exists
	 * 
	 * @param tables The list of tables
	 * @param tableGivenName Full table name exactly as provided by user (same casing, and with schema prefix)
	 * @return True if there is a RelationJSQL with the getGivenName method equals the parameter tableGivenName
	 */
	public static boolean tableIsInList(List<RelationJSQL> tables,
			String tableGivenName) {
		for (RelationJSQL table : tables) {
			if (table.getGivenName().equals(tableGivenName))
				return true;
		}
		return false;
	}
	
	/**
	 * Adds RelationJSQL for all tables referred to by the user supplied foreign keys
	 * 
	 * @param tables The new table names are added to this list
	 * @return The parameter tables is returned, possible extended with new tables
	 */
	public List<RelationJSQL> addReferredTables(List<RelationJSQL> tables){
		for(String tableGivenName : this.referredTables){
			if(!tableIsInList(tables, tableGivenName)){
				String[] tablenames = tableGivenName.split("\\.");
				Table newTable = null;
				if(tablenames.length == 1){
					newTable = new Table(tablenames[0]);
				} else if (tablenames.length == 2){
					newTable = new Table(tablenames[0], tablenames[1]);
				} else {
					log.warn("Too many dots in table name " + tableGivenName + " in user-supplied constraints");
					continue;
				}
				tables.add(new RelationJSQL(new TableJSQL(newTable)));
			}
		}
		return tables;
	}

	/**
	 * Adds the parsed user-supplied constraints to the metadata
	 * @param md
	 */
	public void addConstraints(DBMetadata md){
		this.addFunctionalDependency(md);
		this.addForeignKeys(md);
	}
	
	/**
	 * Inserts the user-supplied primary keys / unique valued columns into the metadata object
	 */
	public void addFunctionalDependency(DBMetadata md) {
		for(String tableName : this.uniqueFD.keySet() ){
			DataDefinition td = md.getDefinition(tableName);
			if(td != null && td instanceof TableDefinition){
				ArrayList<ArrayList<String>> tableFDs = this.uniqueFD.get(tableName);
				//if(tableFDs.size() > 1)
					//log.warn("More than one primary key supplied for table " + tableName + ". Ontop supports only one, so the first is used.");
				for (ArrayList<String> listOfConstraints: tableFDs){
					for (String keyColumn : listOfConstraints){
					int key_pos = td.getAttributeKey(keyColumn);
					
					if(key_pos == -1){
						System.out.println("Column '" + keyColumn + "' not found in table '" + td.getName() + "'");
					} else {
						Attribute attr = td.getAttribute(key_pos);
						if(attr == null){
							log.warn("Error getting attribute " + keyColumn + " from table " + tableName + ". Seems position " + key_pos);
						} else if (! attr.getName().equals(keyColumn)){
							log.warn("Got wrong attribute " + attr.getName() + " when asking for column " + keyColumn + " from table " + tableName);
						} else {		
							//td.setAttribute(key_pos, new Attribute(td, attr.getName(), attr.getType(), false, attr.getSQLTypeName())); // ,/*isUnique*/true
							// ROMAN (17 Aug 2015): do we really change it into NON NULL?
							td.addUniqueConstraint(ImmutableList.of(td.getAttribute(key_pos)));
						}
					}
				}
			}		
					
				md.add(td);
			} else { // no table definition
				log.warn("Error in user supplied primary key: No table definition found for " + tableName + ".");
			}
		}
	}



	/**
	 * Inserts the user-supplied foreign keys / unique valued columns into the metadata object
	 */
	public void addForeignKeys(DBMetadata md) {
		for(String tableName : this.fKeys.keySet() ){
			DataDefinition td = md.getDefinition(tableName);
			if(td == null || ! (td instanceof TableDefinition)){
				log.warn("Error in user-supplied foreign key: Table '" + tableName + "' not found");
				continue;
			}
			ArrayList<HashMap<String, Reference>> tableFKeys = this.fKeys.get(tableName);
			for(HashMap<String, Reference> fKey : tableFKeys){
				for (String keyColumn : fKey.keySet()){
					int key_pos = td.getAttributeKey(keyColumn);
					if(key_pos == -1){
						log.warn("Column '" + keyColumn + "' not found in table '" + td.getName() + "'");
						continue;
					}
					Attribute attr = td.getAttribute(key_pos);
					if(attr == null){
						log.warn("Error getting attribute " + keyColumn + " from table " + tableName + ". Seems position " + key_pos);
						continue;
					}
					if (! attr.getName().equals(keyColumn)){
						log.warn("Got wrong attribute " + attr.getName() + " when asking for column " + keyColumn + " from table " + tableName);
						continue;
					}
					//if(attr.getReference() != null){
					//	log.warn("Manually supplied foreign key ignored since existing in metadata foreign key for '" + td.getName() + "':'" + attr.getName() + "'");
					//	continue;
					//}
					Reference ref = fKey.get(keyColumn);
					String fkTable = ref.getTableReference();
					DataDefinition fktd = md.getDefinition(fkTable);
					if(fktd == null){
						log.warn("Error in user-supplied foreign key: Reference to non-existing table '" + fkTable + "'");
						continue;
					}
					String fkColumn = ref.getColumnReference();
					if(fktd.getAttributeKey(fkColumn) == -1){
						log.warn("Error in user-supplied foreign key: Reference to non-existing column '" + fkColumn + "' in table '" + fkTable + "'");
						continue;
					}
					
					td.addForeignKeyConstraint(new ForeignKeyConstraint.Builder("_FK_" + tableName + "_" + keyColumn)
											.add(attr, fktd.getAttribute(fkColumn)).build());
				}
			}
			md.add(td);
		}
	}
}
