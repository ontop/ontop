/**
 * 
 */
package it.unibz.krdb.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
	
	// List of two-element arrays: table id and a comma-separated list of columns
	private final List<String[]> ucs = new LinkedList<>();
	
	// List of four-element arrays: foreign key table id, comma-separated foreign key columns, 
	//                              primary key (referred) table id, comma-separated primary key columns
	private final List<String[]> fks = new LinkedList<>();

	
	/**
	 * Reads colon separated pairs of view name and primary key
	 * @param filename The name of the plain-text file with the fake keys
	 * @throws IOException 
	 */
	public ImplicitDBConstraints(String filename) {
		this(new File(filename));
	}

	/**
	 * Reads colon-separated pairs of view name and primary key
	 * 
	 * @param file The plain-text file with functional dependencies
	 * 
	 * @throws IOException 
	 */
	public ImplicitDBConstraints(File file) {
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(":");
				if (parts.length == 2) { // Primary Key		
					ucs.add(parts);
				} 
				else if (parts.length == 4) { // Foreign Key
					fks.add(parts);
				}
			}
		} 
		catch (FileNotFoundException e) {
			log.warn("Could not find file " + file + " in directory " + System.getenv().get("PWD"));
			String currentDir = System.getProperty("user.dir");
			log.warn("Current dir using System:" + currentDir);
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
	public Set<RelationID> getReferredTables(QuotedIDFactory idfac) {
		Set<RelationID> referredTables = new HashSet<>();

		for (String[] fk : fks) {
			RelationID fkTableId = getRelationIDFromString(fk[2], idfac);
			referredTables.add(fkTableId);
		}
		
		return referredTables;
	}

	private RelationID getRelationIDFromString(String name, QuotedIDFactory idfac) {
		String[] names = name.split("\\.");
		if (names.length == 1)
			return idfac.createRelationID(null, name);
		else
			return idfac.createRelationID(names[0], names[1]);			
	}
	
	/**
	 * Inserts the user-supplied primary keys / unique valued columns into the metadata object
	 */
	public void addFunctionalDependencies(DBMetadata md) {
		QuotedIDFactory idfac = md.getQuotedIDFactory();
		
		for (String[] uc : ucs) {
			RelationID tableId = getRelationIDFromString(uc[0], idfac);
			DatabaseRelationDefinition td = md.getDatabaseRelation(tableId);

			if (td != null) {
				UniqueConstraint.Builder builder = UniqueConstraint.builder(td);
				String[] columns = uc[1].split(",");
				for (String column : columns) {
					QuotedID columnId = idfac.createAttributeID(column);
					Attribute attr = td.getAttribute(columnId);
					if (attr != null) {
						//td.setAttribute(key_pos, new Attribute(td, attr.getName(), attr.getType(), false, attr.getSQLTypeName())); // true
						// ROMAN (17 Aug 2015): do we really change it into NON NULL?
						builder.add(attr);
					} 
					else 		
						log.warn("Column '" + columnId + "' not found in table '" + td.getID() + "'");
				}
				td.addUniqueConstraint(builder.build("UC_NAME", false));
			} 
			else 
				log.warn("Error in user supplied primary key: No table definition found for " + tableId + ".");
		}		
	}



	/**
	 * Inserts the user-supplied foreign keys / unique valued columns into the metadata object
	 */
	public void addForeignKeys(DBMetadata md) {
		QuotedIDFactory idfac = md.getQuotedIDFactory();
				
		for (String[] fk : fks) {
			RelationID pkTableId = getRelationIDFromString(fk[2], idfac);
			DatabaseRelationDefinition pkTable = md.getDatabaseRelation(pkTableId);
			if (pkTable == null) {
				log.warn("Error in user-supplied foreign key: " + pkTableId + " not found");
				continue;
			}
			RelationID fkTableId = getRelationIDFromString(fk[0], idfac);
			DatabaseRelationDefinition fkTable = md.getDatabaseRelation(fkTableId);
			if (fkTable == null) {
				log.warn("Error in user-supplied foreign key: " + fkTableId + " not found");
				continue;
			}
			String[] pkColumns = fk[3].split(",");
			String[] fkColumns = fk[1].split(",");
			if (fkColumns.length != pkColumns.length) {
				log.warn("Compound foreign key refers to different number of columns: " + fk);
				continue;
			}
			
			ForeignKeyConstraint.Builder fkBuilder = ForeignKeyConstraint.builder(fkTable, pkTable);
			for (int i = 0; i < pkColumns.length; i++) {
				QuotedID pkColumnId = idfac.createAttributeID(pkColumns[i]);
				Attribute pkAttr = pkTable.getAttribute(pkColumnId);
				if (pkAttr == null) {
					log.warn("Error in user-supplied foreign key: " + pkColumnId + " in table " + pkTable);
					continue;
				}
				QuotedID fkColumnId = idfac.createAttributeID(fkColumns[i]);
				Attribute fkAttr = fkTable.getAttribute(fkColumnId);
				if (fkAttr == null) {
					log.warn("Error in user-supplied foreign key: " + fkColumnId + " in table " + fkTable);
					continue;
				}
				
				fkBuilder.add(fkAttr, pkAttr);
			}
			fkTable.addForeignKeyConstraint(
					fkBuilder.build(fkTable.getID().getTableName() + "_FK_" + pkTable.getID().getTableName()));
		}		
	}
}
