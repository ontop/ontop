/**
 * 
 */
package it.unibz.krdb.sql;

import it.unibz.krdb.sql.api.Attribute;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Dag Hovland
 *
 * Used for reading user-provided information about keys in views and materialized views. 
 * Necessary for better performance in cases where materialized views do a lot of work
 *
 */
public class ViewKeyReader {
	/**
	 * Reads colon separated pairs of view name and primary key
	 * @param The metadata, already populated
	 * @param The name of the plain-text file with the fake keys
	 * @throws IOException 
	 */
	public static void addViewKeys(DBMetadata md, String filename) throws IOException{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(":");
				if (parts.length >= 2){ // Primary or foreign key
					String tableName = parts[0];
					String keyColumn = parts[1];
					DataDefinition td = md.getDefinition(tableName);
					if(td != null && td instanceof TableDefinition){
						int key_pos = td.getAttributeKey(keyColumn);
						if(key_pos == -1){
							System.out.println("Column '" + keyColumn + "' not found in table '" + td.getName() + "'");
						} else {
							Attribute attr = td.getAttribute(key_pos);
							if(attr == null){
								System.out.println("Error getting attribute " + keyColumn + " from table " + tableName + ". Seems position " + key_pos);
							} else if (! attr.getName().equals(keyColumn)){
								System.out.println("Got wrong attribute " + attr.getName() + " when asking for column " + keyColumn + " from table " + tableName);
							} else {
								if(parts.length == 2) { // Primary key		
									td.setAttribute(key_pos, new Attribute(attr.getName(), attr.getType(), true, attr.getReference(), 0));
								} else if (parts.length == 4){ // FOreign key
									if(attr.getReference() != null){
										System.out.println("Manually supplied foreign key ignored since existing in metadata foreign key for '" + td.getName() + "':'" + attr.getName() + "'");
										continue;
									}
									String fkTable = parts[2];
									DataDefinition fktd = md.getDefinition(fkTable);
									if(fktd == null){
										System.out.println("Error in foreign key: Reference to non-existing table '" + fkTable + "'");
										continue;
									}
									String fkColumn = parts[3];
									if(fktd.getAttributeKey(fkColumn) == -1){
										System.out.println("Error in foreign key: Reference to non-existing column '" + fkColumn + "' in table '" + fkTable + "'");
										continue;
									}
									String fkName = keyColumn + fkTable;
									Reference ref = new Reference(fkName, fkTable, fkColumn);
									td.setAttribute(key_pos, new Attribute(attr.getName(), attr.getType(), attr.isPrimaryKey() , ref, attr.canNull() ? 1 : 0));
								}
								md.add(td);
								System.out.println("Changed metadata about " + td.getName());
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file " + filename + " in directory " + System.getenv().get("PWD"));
			String current = new java.io.File( "." ).getCanonicalPath();
			System.out.println("Current dir:"+current);
			String currentDir = System.getProperty("user.dir");
			System.out.println("Current dir using System:" +currentDir);
		} catch (IOException e) {
			System.out.println("Problem reading keys from  file " + filename);
			e.printStackTrace();
		} 
		
	}
}
