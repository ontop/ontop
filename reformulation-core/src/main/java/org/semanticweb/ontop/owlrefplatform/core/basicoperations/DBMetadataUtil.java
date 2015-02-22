package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

/*
 * #%L
 * ontop-reformulation-core
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.Function;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.Predicate;
import org.semanticweb.ontop.model.Term;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.Reference;
import org.semanticweb.ontop.sql.TableDefinition;
import org.semanticweb.ontop.sql.api.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBMetadataUtil {

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	
	private static Logger log = LoggerFactory.getLogger(DBMetadataUtil.class);
	
	private static final String variableSuffix = "_4022013_";
	
	/*
	 * generate CQIE rules from foreign key info of db metadata
	 * TABLE1.COL1 references TABLE2.COL2 as foreign key then 
	 * construct CQIE rule TABLE2(P1, P3, COL2, P4) :- TABLE1(COL2, T2, T3).
	 */
	public static LinearInclusionDependencies generateFKRules(DBMetadata metadata) {
		LinearInclusionDependencies dependencies = new LinearInclusionDependencies();
		
		List<TableDefinition> tableDefs = metadata.getTableList();
		for (TableDefinition def : tableDefs) {
			Map<String, List<Attribute>> foreignKeys = def.getForeignKeys();
			for (Entry<String, List<Attribute>> fks : foreignKeys.entrySet()) {
				String fkName = fks.getKey();
				List<Attribute> fkAttributes = fks.getValue();
				try {
					String table1 = def.getName();
					String table2 = "";
					TableDefinition def2 = null;
					Map<Integer, Integer> positionMatch = new HashMap<Integer, Integer>();
					for (Attribute attr : fkAttributes) {
						// Get current table and column (1)
						String column1 = attr.getName();
						
						// Get referenced table and column (2)
						Reference reference = attr.getReference();
						table2 = reference.getTableReference();
						String column2 = reference.getColumnReference();				
						
						// Get table definition for referenced table
						def2 = (TableDefinition) metadata.getDefinition(table2);
						if (def2 == null) { // in case of broken FK
							// ROMAN: this is not necessarily broken -- the table may not be mentioned in the mappings 
							//        (which can happen in the NEW abridged metadata)
							throw new BrokenForeignKeyException(reference, "Missing table: " + table2);
						}
						// Get positions of referenced attribute
						int pos1 = def.getAttributePosition(column1);
						if (pos1 == -1) {
							throw new BrokenForeignKeyException(reference, "Missing column: " + column1);
						}
						int pos2 = def2.getAttributePosition(column2);
						if (pos2 == -1) {
							throw new BrokenForeignKeyException(reference, "Missing column: " + column2);
						}
						positionMatch.put(pos1, pos2);
					}
					// Construct CQIE
					Predicate p1 = fac.getPredicate(table1, def.getNumOfAttributes());					
					List<Term> terms1 = new ArrayList<Term>(def.getNumOfAttributes());
					for (int i=0; i < def.getNumOfAttributes(); i++) {
						 terms1.add(fac.getVariable("t" + variableSuffix + (i+1)));
					}
					
					Predicate p2 = fac.getPredicate(table2, def2.getNumOfAttributes());
					List<Term> terms2 = new ArrayList<Term>(def2.getNumOfAttributes());
					for (int i=0; i < def2.getNumOfAttributes(); i++) {
						 terms2.add(fac.getVariable("p" + variableSuffix + (i+1)));
					}
					// Do the swapping
					for (Entry<Integer,Integer> swap : positionMatch.entrySet()) 
						terms1.set(swap.getKey(), terms2.get(swap.getValue()));
					
					Function head = fac.getFunction(p2, terms2);
					Function body = fac.getFunction(p1, terms1);
					
					dependencies.addRule(head, body);				
				} 
				catch (BrokenForeignKeyException e) {
					// Log the warning message
					log.warn(e.getMessage());
				}
			}
		}		
		return dependencies;
	}
}
